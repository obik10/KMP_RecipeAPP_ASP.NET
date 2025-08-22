package org.robiul.kmprecipeapp.domain.repository

import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.utils.Result

interface RecipeRepository {
    suspend fun getRecipes(pageNumber: Int, pageSize: Int): Result<List<Recipe>>
    suspend fun searchRecipes(keyword: String): Result<List<Recipe>>

    // additional methods
    suspend fun getRecipeById(id: String): Result<Recipe>
    suspend fun createRecipe(recipe: Recipe): Result<Recipe>
    suspend fun updateRecipe(id: String, recipe: Recipe): Result<Recipe>
    suspend fun deleteRecipe(id: String): Result<Unit>

    suspend fun uploadRecipeImage(id: String, fileName: String, bytes: ByteArray): Result<Recipe>

    suspend fun getMyRecipes(): Result<List<Recipe>>
    suspend fun addFavorite(id: String): Result<Recipe>
    suspend fun removeFavorite(id: String): Result<Recipe>
    suspend fun getMyFavorites(): Result<List<Recipe>>
}
