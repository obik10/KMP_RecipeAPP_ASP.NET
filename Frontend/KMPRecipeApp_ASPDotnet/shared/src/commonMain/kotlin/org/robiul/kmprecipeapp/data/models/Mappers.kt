package org.robiul.kmprecipeapp.data.models

import org.robiul.kmprecipeapp.data.models.dto.*
import org.robiul.kmprecipeapp.domain.models.Ingredient
import org.robiul.kmprecipeapp.domain.models.Recipe

// --- DTO → Domain ---

fun RecipeDto.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    instructions = instructions,
    ownerId = ownerId,
    imagePath = imagePath,
    ingredients = ingredients.map { it.toDomain() },
    youtubeUrl = youtubeUrl,
    isExternal = isExternal
)

fun RecipeIngredientDto.toDomain(): Ingredient = Ingredient(
    name = name,
    measure = measure
)

// List helpers (renamed to avoid JVM signature clash)
fun List<RecipeDto>.toRecipeDomainList(): List<Recipe> = map { it.toDomain() }
fun List<RecipeIngredientDto>.toIngredientDomainList(): List<Ingredient> = map { it.toDomain() }

// --- Domain → DTO / Requests ---

fun Ingredient.toRecipeIngredientDto(): RecipeIngredientDto = RecipeIngredientDto(
    name = name,
    measure = measure
)

fun Ingredient.toIngredientRequest(): IngredientRequest = IngredientRequest(
    name = name,
    measure = measure
)

fun Recipe.toCreateRequest(): CreateRecipeRequest = CreateRecipeRequest(
    title = title,
    instructions = instructions,
    ownerId = ownerId,
    ingredients = ingredients.map { it.toIngredientRequest() },
    youtubeUrl = youtubeUrl.orEmpty()
)

fun Recipe.toUpdateRequest(): UpdateRecipeRequest = UpdateRecipeRequest(
    title = title,
    instructions = instructions,
    ingredients = ingredients.map { it.toIngredientRequest() },
    youtubeUrl = youtubeUrl.orEmpty()
)

// List helpers for domain -> dto (already unique)
fun List<Ingredient>.toIngredientRequestList(): List<IngredientRequest> = map { it.toIngredientRequest() }
fun List<Ingredient>.toRecipeIngredientDtoList(): List<RecipeIngredientDto> = map { it.toRecipeIngredientDto() }
fun List<Recipe>.toCreateRequestList(): List<CreateRecipeRequest> = map { it.toCreateRequest() }
fun List<Recipe>.toUpdateRequestList(): List<UpdateRecipeRequest> = map { it.toUpdateRequest() }
