package org.robiul.kmprecipeapp.presentation.mapper

import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.presentation.model.RecipeUiModel

fun Recipe.toUiModel(): RecipeUiModel {
    return RecipeUiModel(
        id = this.id ?: "",
        title = this.title,
        imageUrl = this.imagePath.orEmpty()
    )
}
