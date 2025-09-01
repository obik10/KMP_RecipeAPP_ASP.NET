// file: org/robiul/kmprecipeapp/presentation/viewmodel/MyRecipesEditViewModel.kt
package org.robiul.kmprecipeapp.presentation.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlin.random.Random
import org.robiul.kmprecipeapp.domain.models.Ingredient
import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.utils.Result

class MyRecipesEditViewModel(
    private val repository: RecipeRepository,
    private val recipeId: String
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var title by mutableStateOf("")
    var instructions by mutableStateOf("")
    var imageUrl by mutableStateOf<String?>(null)
    var youtubeUrl by mutableStateOf("")

    var ingredients by mutableStateOf(listOf<Ingredient>())

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    var currentRecipe by mutableStateOf<Recipe?>(null)
        private set

    private fun genId(): String = buildString(16) {
        val hex = "0123456789abcdef"
        repeat(16) { append(hex[Random.nextInt(hex.length)]) }
    }

    fun loadRecipe() {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            when (val res = repository.getRecipeById(recipeId)) {
                is Result.Success -> {
                    currentRecipe = res.data
                    title = res.data.title
                    instructions = res.data.instructions
                    imageUrl = res.data.imagePath
                    youtubeUrl = res.data.youtubeUrl ?: ""

                    // ✅ assign immutable list with ids
                    ingredients = res.data.ingredients.map { ing ->
                        Ingredient(
                            id = genId(),
                            name = ing.name,
                            measure = ing.measure
                        )
                    }
                }
                is Result.Error -> errorMessage = "Error loading recipe: ${res.error}"
            }
            isLoading = false
        }
    }

    private fun validate(): Boolean {
        if (title.isBlank()) { errorMessage = "Title cannot be empty"; return false }
        if (instructions.isBlank()) { errorMessage = "Instructions cannot be empty"; return false }
        if (ingredients.none { it.name.isNotBlank() }) {
            errorMessage = "Add at least one ingredient with a name"; return false
        }
        val yt = youtubeUrl.trim()
        if (yt.isNotEmpty() && !isValidYouTubeUrl(yt)) {
            errorMessage = "Invalid YouTube URL"; return false
        }
        return true
    }

    fun updateRecipe(onSuccess: () -> Unit) {
        if (!validate()) return

        val yt = youtubeUrl.trim().ifBlank { null }

        val recipe = currentRecipe?.copy(
            title = title.trim(),
            instructions = instructions.trim(),
            imagePath = imageUrl?.trim(),
            youtubeUrl = yt,
            ingredients = ingredients.map {
                it.copy(
                    name = it.name.trim(),
                    measure = it.measure.trim()
                )
            }
        ) ?: run {
            errorMessage = "No recipe loaded"
            return
        }

        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            when (val res = repository.updateRecipe(recipeId, recipe)) {
                is Result.Success -> {
                    currentRecipe = res.data
                    successMessage = "Recipe updated"
                    onSuccess()
                }
                is Result.Error -> errorMessage = "Error updating recipe: ${res.error}"
            }
            isLoading = false
        }
    }



    fun uploadImage(fileName: String, bytes: ByteArray, onSuccess: (() -> Unit)? = null) {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            when (val res = repository.uploadRecipeImage(recipeId, fileName, bytes)) {
                is Result.Success -> {
                    currentRecipe = res.data
                    imageUrl = res.data.imagePath
                    successMessage = "Image uploaded"
                    onSuccess?.invoke()
                }
                is Result.Error -> errorMessage = "Image upload failed: ${res.error}"
            }
            isLoading = false
        }
    }

    fun deleteRecipe(onSuccess: () -> Unit) {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            when (val res = repository.deleteRecipe(recipeId)) {
                is Result.Success -> onSuccess()
                is Result.Error -> errorMessage = "Error deleting recipe: ${res.error}"
            }
            isLoading = false
        }
    }

    private fun isValidYouTubeUrl(url: String): Boolean {
        val u = url.lowercase()
        return u.startsWith("https://www.youtube.com/") ||
                u.startsWith("http://www.youtube.com/") ||
                u.startsWith("https://youtube.com/") ||
                u.startsWith("http://youtube.com/") ||
                u.startsWith("https://youtu.be/") ||
                u.startsWith("http://youtu.be/")
    }

    fun addIngredient() {
        ingredients = ingredients + Ingredient(id = genId(), name = "", measure = "")
    }

    fun updateIngredientById(id: String, newIngredient: Ingredient) {
        ingredients = ingredients.map {
            if (it.id == id) newIngredient else it
        }
    }

    fun removeIngredientById(id: String) {
        ingredients = ingredients.filterNot { it.id == id }
    }

    fun updateIngredient(index: Int, newIngredient: Ingredient) {
        ingredients = ingredients.toMutableList().also { list ->
            if (index in list.indices) list[index] = newIngredient
        }
    }

    fun removeIngredient(index: Int) {
        ingredients = ingredients.toMutableList().also { list ->
            if (index in list.indices) list.removeAt(index)
        }
    }

}
