// org.robiul.kmprecipeapp.presentation.model.RecipeUiModel.kt
package org.robiul.kmprecipeapp.presentation.model

import org.robiul.kmprecipeapp.domain.models.Recipe

data class RecipeUiModel(
    val id: String,
    val title: String,
    val imageUrl: String?
)

// mapper
fun RecipeUiModel.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    instructions = "",
    imagePath = imageUrl,
    ownerId = null,
    ingredients = emptyList(),
    youtubeUrl = null,
    isExternal = false
)
