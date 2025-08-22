package org.robiul.kmprecipeapp.domain.models

data class Recipe(
    val id: String,
    val title: String,
    val instructions: String,
    val ownerId: String,
    val imagePath: String? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val youtubeUrl: String? = null,
    val isExternal: Boolean = false
)
