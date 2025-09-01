package org.robiul.kmprecipeapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.robiul.kmprecipeapp.domain.models.Ingredient
import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.utils.getOrThrow

class MyRecipesAddViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    private var pendingImage: Pair<String, ByteArray>? = null

    /**
     * Store selected image in memory until recipe is created.
     */
    fun setImage(fileName: String, bytes: ByteArray) {
        pendingImage = fileName to bytes
    }

    /**
     * Save recipe to backend (and upload image if selected).
     */
    fun saveRecipe(
        title: String,
        instructions: String,
        youtubeUrl: String?,
        ingredients: List<Ingredient>,
        onSaved: () -> Unit
    ) {
        if (title.isBlank() || instructions.isBlank()) {
            errorMessage = "Title and instructions cannot be empty"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Step 1: create recipe without image
                val recipeResult = repository.createRecipe(
                    Recipe(
                        id = "",
                        title = title,
                        instructions = instructions,
                        imagePath = null,
                        ownerId = null,
                        ingredients = ingredients,
                        youtubeUrl = youtubeUrl,
                        isExternal = false
                    )
                )

                val createdRecipe = recipeResult.getOrThrow()

                // Step 2: if user picked an image, upload it
                pendingImage?.let { (fileName, bytes) ->
                    val uploadResult = repository.uploadRecipeImage(
                        createdRecipe.id!!, fileName, bytes
                    )
                    uploadResult.getOrThrow() // updated recipe with image
                }

                successMessage = "Recipe created!"
                onSaved()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}
