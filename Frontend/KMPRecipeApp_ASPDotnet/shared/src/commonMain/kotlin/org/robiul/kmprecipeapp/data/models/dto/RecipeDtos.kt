package org.robiul.kmprecipeapp.data.models.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class IngredientRequest(
    val name: String,
    val measure: String
)

@Serializable
data class CreateRecipeRequest(
    val title: String,
    val instructions: String,
    val ownerId: String,
    val ingredients: List<IngredientRequest>,
    val youtubeUrl: String
)

@Serializable
data class UpdateRecipeRequest(
    val title: String,
    val instructions: String,
    val ingredients: List<IngredientRequest>,
    val youtubeUrl: String
)

@Serializable
data class RecipeIngredientDto(
    val name: String,
    val measure: String
)

@Serializable
data class RecipeDto(
    val id: String,
    val isExternal: Boolean,
    val title: String,
    val instructions: String,
    val ownerId: String,
    val imagePath: String? = null,
    val ingredients: List<RecipeIngredientDto> = emptyList(),
    val youtubeUrl: String? = null
)

@Serializable
data class RecipeDtoPaginatedResult(
    val items: List<RecipeDto>,
    val totalCount: Int,
    val pageNumber: Int,
    val pageSize: Int,
    val totalPages: Int? = null
)
