package org.robiul.kmprecipeapp.data.datasource

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.robiul.kmprecipeapp.db.AppDatabase
import org.robiul.kmprecipeapp.db.Recipes
import org.robiul.kmprecipeapp.domain.models.Ingredient
import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.utils.AppError
import org.robiul.kmprecipeapp.utils.Result

class LocalDataSource(driver: SqlDriver) {
    private val db = AppDatabase(driver)
    private val recipeQueries = db.recipesQueries
    private val ingredientQueries = db.recipeIngredientsQueries

    // Clear all recipes and ingredients
    suspend fun clearAll(): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            db.transaction {
                recipeQueries.deleteAllRecipes()
                ingredientQueries.deleteAllIngredients()
            }
            Result.Success(Unit)
        } catch (t: Throwable) {
            Result.Error(AppError.Db(t.message, t))
        }
    }

    // Cache recipes locally
    suspend fun cacheRecipes(recipes: List<Recipe>): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            db.transaction {
                recipes.forEach { r ->
                    recipeQueries.insertOrReplaceRecipe(
                        id = r.id,
                        title = r.title,
                        instructions = r.instructions,
                        ownerId = r.ownerId,
                        imagePath = r.imagePath,
                        isExternal = if (r.isExternal) 1 else 0L,
                        youtubeUrl = r.youtubeUrl
                    )
                    ingredientQueries.deleteIngredientsByRecipeId(r.id)
                    r.ingredients.forEach { i ->
                        ingredientQueries.insertIngredient(
                            recipeId = r.id,
                            name = i.name,
                            measure = i.measure
                        )
                    }
                }
            }
            Result.Success(Unit)
        } catch (t: Throwable) {
            Result.Error(AppError.Db(t.message, t))
        }
    }

    // Get all recipes (public + user)
    suspend fun getAllRecipes(): Result<List<Recipe>> =
        fetchRecipes(recipeQueries.selectAllRecipes())

    // Get only public recipes (ownerId IS NULL)
    suspend fun getPublicRecipes(): Result<List<Recipe>> =
        fetchRecipes(recipeQueries.selectPublicRecipes())

    // Get recipes for a specific user
    suspend fun getUserRecipes(userId: String): Result<List<Recipe>> =
        fetchRecipes(recipeQueries.selectUserRecipes(userId))

    // Get single recipe by id
    suspend fun getRecipeById(id: String): Result<Recipe> =
        fetchRecipe(recipeQueries.selectRecipeById(id))

    // Search recipes (all)
    suspend fun searchRecipes(keyword: String): Result<List<Recipe>> =
        fetchRecipes(recipeQueries.searchRecipesByTitle("%$keyword%"))

    // Search only public recipes
    suspend fun searchPublicRecipes(keyword: String): Result<List<Recipe>> =
        fetchRecipes(recipeQueries.searchPublicRecipesByTitle("%$keyword%"))

    // Search only user recipes
    suspend fun searchUserRecipes(keyword: String, userId: String): Result<List<Recipe>> =
        fetchRecipes(recipeQueries.searchUserRecipesByTitle("%$keyword%", userId))

    // Internal helper to fetch multiple recipes
    private suspend fun fetchRecipes(query: Query<Recipes>): Result<List<Recipe>> =
        withContext(Dispatchers.Default) {
            try {
                val rows = query.executeAsList()
                val data = rows.map { row ->
                    val ings = ingredientQueries
                        .selectIngredientsByRecipeId(row.id)
                        .executeAsList()
                        .map { Ingredient(it.name, it.measure) }

                    Recipe(
                        id = row.id,
                        title = row.title,
                        instructions = row.instructions,
                        ownerId = row.ownerId,
                        imagePath = row.imagePath,
                        ingredients = ings,
                        youtubeUrl = row.youtubeUrl,
                        isExternal = row.isExternal != 0L
                    )
                }
                Result.Success(data)
            } catch (t: Throwable) {
                Result.Error(AppError.Db(t.message, t))
            }
        }

    // Internal helper to fetch single recipe
    private suspend fun fetchRecipe(query: Query<Recipes>): Result<Recipe> =
        withContext(Dispatchers.Default) {
            try {
                val row = query.executeAsOneOrNull() ?: return@withContext Result.Error(AppError.NotFound)
                val ings = ingredientQueries
                    .selectIngredientsByRecipeId(row.id)
                    .executeAsList()
                    .map { Ingredient(it.name, it.measure) }

                Result.Success(
                    Recipe(
                        id = row.id,
                        title = row.title,
                        instructions = row.instructions,
                        ownerId = row.ownerId,
                        imagePath = row.imagePath,
                        ingredients = ings,
                        youtubeUrl = row.youtubeUrl,
                        isExternal = row.isExternal != 0L
                    )
                )
            } catch (t: Throwable) {
                Result.Error(AppError.Db(t.message, t))
            }
        }
}
