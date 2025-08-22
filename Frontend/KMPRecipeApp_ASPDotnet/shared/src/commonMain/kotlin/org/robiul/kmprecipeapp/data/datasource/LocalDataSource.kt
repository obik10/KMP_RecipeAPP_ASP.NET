package org.robiul.kmprecipeapp.data.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.robiul.kmprecipeapp.db.AppDatabase
import org.robiul.kmprecipeapp.domain.models.Ingredient
import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.utils.AppError
import org.robiul.kmprecipeapp.utils.Result

class LocalDataSource(
    driver: SqlDriver
) {
    private val db = AppDatabase(driver)
    private val recipeQueries = db.recipesQueries
    private val ingredientQueries = db.recipeIngredientsQueries

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

    suspend fun getAllRecipes(): Result<List<Recipe>> = withContext(Dispatchers.Default) {
        try {
            val rows = recipeQueries.selectAllRecipes().executeAsList()
            val data = rows.map { row ->
                val ings = ingredientQueries.selectIngredientsByRecipeId(row.id).executeAsList().map {
                    Ingredient(name = it.name, measure = it.measure)
                }
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

    suspend fun getRecipeById(id: String): Result<Recipe> = withContext(Dispatchers.Default) {
        try {
            val row = recipeQueries.selectRecipeById(id).executeAsOneOrNull()
                ?: return@withContext Result.Error(AppError.NotFound)
            val ings = ingredientQueries.selectIngredientsByRecipeId(id).executeAsList().map {
                Ingredient(name = it.name, measure = it.measure)
            }
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

    suspend fun searchRecipes(keyword: String): Result<List<Recipe>> = withContext(Dispatchers.Default) {
        try {
            val rows = recipeQueries.searchRecipesByTitle("%$keyword%").executeAsList()
            val data = rows.map { row ->
                val ings = ingredientQueries.selectIngredientsByRecipeId(row.id).executeAsList().map {
                    Ingredient(name = it.name, measure = it.measure)
                }
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
}
