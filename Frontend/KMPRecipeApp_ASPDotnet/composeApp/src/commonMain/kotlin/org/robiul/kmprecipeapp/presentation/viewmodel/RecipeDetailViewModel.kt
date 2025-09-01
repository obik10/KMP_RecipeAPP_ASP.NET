package org.robiul.kmprecipeapp.presentation.viewmodel

import RecipeDetailState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.domain.usecase.GetRecipeById
import org.robiul.kmprecipeapp.utils.Result

class RecipeDetailViewModel(
    private val getRecipeById: GetRecipeById
) {
    var state by mutableStateOf(RecipeDetailState())
        private set

    private val scope = CoroutineScope(Dispatchers.Default)

    fun loadRecipe(id: String) {
        state = state.copy(isLoading = true, errorMessage = null)

        scope.launch {
            when (val result = getRecipeById(id)) {
                is Result.Success -> {
                    state = state.copy(
                        isLoading = false,
                        recipe = result.data,
                        isFavorite = false // default until backend is ready
                    )
                }
                is Result.Error -> {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        state = state.copy(isFavorite = !state.isFavorite)
        // TODO: persist this later (backend / local DB)
    }
}
