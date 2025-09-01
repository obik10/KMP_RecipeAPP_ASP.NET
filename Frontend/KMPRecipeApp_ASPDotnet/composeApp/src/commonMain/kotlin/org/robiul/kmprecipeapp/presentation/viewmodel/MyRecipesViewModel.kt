package org.robiul.kmprecipeapp.presentation.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.robiul.kmprecipeapp.domain.usecase.GetMyRecipes
import org.robiul.kmprecipeapp.presentation.mapper.toUiModel
import org.robiul.kmprecipeapp.presentation.state.MyRecipesUiState
import org.robiul.kmprecipeapp.utils.Result

class MyRecipesViewModel(
    private val getMyRecipes: GetMyRecipes
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var state by mutableStateOf(MyRecipesUiState())
        private set

    fun loadMyRecipes() {
        scope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            try {
                when (val res = getMyRecipes()) { // Result<List<Recipe>>
                    is Result.Success -> {
                        state = state.copy(
                            isLoading = false,
                            recipes = res.data.map { it.toUiModel() }
                        )
                    }
                    is Result.Error -> {
                        state = state.copy(
                            isLoading = false,
                            errorMessage =  "Failed to load recipes"
                        )
                    }
                }
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }
}
