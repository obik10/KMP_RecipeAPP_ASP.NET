package org.robiul.kmprecipeapp.presentation.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.robiul.kmprecipeapp.domain.usecase.GetRecipesPaginated
import org.robiul.kmprecipeapp.domain.usecase.SearchRecipes
import org.robiul.kmprecipeapp.presentation.mapper.toUiModel
import org.robiul.kmprecipeapp.presentation.state.HomeUiState
import org.robiul.kmprecipeapp.utils.Result

class HomeViewModel(
    private val getRecipes: GetRecipesPaginated,
    private val searchRecipesUseCase: SearchRecipes
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var state by mutableStateOf(HomeUiState())
        private set

    fun loadPage(page: Int) {
        scope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            when (val result = getRecipes(page, 10)) {
                is Result.Success -> {
                    state = state.copy(
                        isLoading = false,
                        recipes = result.data.items.map { it.toUiModel() },
                        currentPage = result.data.pageNumber,
                        totalPages = result.data.totalPages ?: 1
                    )
                }
                is Result.Error -> {
                    state = state.copy(isLoading = false, errorMessage = result.error.message)
                }
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            // fallback to first page
            loadPage(1)
            return
        }

        scope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            when (val result = searchRecipesUseCase(query)) {
                is Result.Success -> {
                    state = state.copy(
                        isLoading = false,
                        recipes = result.data.map { it.toUiModel() },
                        currentPage = 1,
                        totalPages = 1
                    )
                }
                is Result.Error -> {
                    state = state.copy(isLoading = false, errorMessage = result.error.message)
                }
            }
        }
    }
}
