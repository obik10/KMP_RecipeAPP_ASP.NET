package org.robiul.kmprecipeapp.presentation.state

import org.robiul.kmprecipeapp.domain.models.Recipe

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val recipes: List<Recipe> = emptyList(),
    val errorMessage: String? = null
)
