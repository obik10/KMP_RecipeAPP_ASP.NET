package org.robiul.kmprecipeapp.presentation.state

import org.robiul.kmprecipeapp.presentation.model.RecipeUiModel

data class MyRecipesUiState(
    val isLoading: Boolean = false,
    val recipes: List<RecipeUiModel> = emptyList(),
    val errorMessage: String? = null
)
