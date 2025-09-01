package org.robiul.kmprecipeapp.presentation.state

import org.robiul.kmprecipeapp.presentation.model.RecipeUiModel

data class HomeUiState(
    val isLoading: Boolean = false,
    val recipes: List<RecipeUiModel> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val errorMessage: String? = null
)