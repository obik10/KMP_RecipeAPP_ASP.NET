package org.robiul.kmprecipeapp.data.repository

import org.robiul.kmprecipeapp.data.datasource.LocalDataSource
import org.robiul.kmprecipeapp.data.datasource.RemoteDataSource
import org.robiul.kmprecipeapp.data.models.dto.CreateRecipeRequest
import org.robiul.kmprecipeapp.data.models.dto.IngredientRequest
import org.robiul.kmprecipeapp.data.models.dto.UpdateRecipeRequest
import org.robiul.kmprecipeapp.data.models.toDomain
import org.robiul.kmprecipeapp.domain.models.Ingredient
import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.utils.Result

class RecipeRepositoryImpl(
    private val remote: RemoteDataSource,
    private val local: LocalDataSource
) : RecipeRepository {

    override suspend fun getRecipes(pageNumber: Int, pageSize: Int): Result<List<Recipe>> {
        when (val net = remote.listPaginated(pageNumber, pageSize)) {
            is Result.Success -> {
                val domain = net.data.items.map { it.toDomain() }
                // cache
                local.cacheRecipes(domain)
                return Result.Success(domain)
            }
            is Result.Error -> {
                val cache = local.getAllRecipes()
                return if (cache is Result.Success && cache.data.isNotEmpty()) cache
                else Result.Error(net.error)
            }
        }
    }

    override suspend fun searchRecipes(keyword: String): Result<List<Recipe>> {
        when (val net = remote.search(keyword)) {
            is Result.Success -> {
                val domain = net.data.map { it.toDomain() }
                local.cacheRecipes(domain) // optional refresh
                return Result.Success(domain)
            }
            is Result.Error -> {
                val cache = local.searchRecipes(keyword)
                return if (cache is Result.Success && cache.data.isNotEmpty()) cache
                else Result.Error(net.error)
            }
        }
    }

    override suspend fun getRecipeById(id: String): Result<Recipe> {
        when (val net = remote.getById(id)) {
            is Result.Success -> {
                val domain = net.data.toDomain()
                local.cacheRecipes(listOf(domain))
                return Result.Success(domain)
            }
            is Result.Error -> {
                val cache = local.getRecipeById(id)
                return if (cache is Result.Success) cache else Result.Error(net.error)
            }
        }
    }

    override suspend fun createRecipe(recipe: Recipe): Result<Recipe> {
        val req = toCreateRequest(recipe)
        return when (val net = remote.create(req)) {
            is Result.Success -> {
                val domain = net.data.toDomain()
                // optionally cache created recipe
                local.cacheRecipes(listOf(domain))
                Result.Success(domain)
            }
            is Result.Error -> Result.Error(net.error)
        }
    }

    override suspend fun updateRecipe(id: String, recipe: Recipe): Result<Recipe> {
        val req = toUpdateRequest(recipe)
        return when (val net = remote.update(id, req)) {
            is Result.Success -> {
                val domain = net.data.toDomain()
                local.cacheRecipes(listOf(domain))
                Result.Success(domain)
            }
            is Result.Error -> Result.Error(net.error)
        }
    }

    override suspend fun deleteRecipe(id: String): Result<Unit> {
        return when (val net = remote.delete(id)) {
            is Result.Success -> {
                // optionally remove from local cache (clear all/refresh)
                Result.Success(Unit)
            }
            is Result.Error -> Result.Error(net.error)
        }
    }

    override suspend fun uploadRecipeImage(id: String, fileName: String, bytes: ByteArray): Result<Recipe> {
        return when (val net = remote.uploadImage(id, fileName, bytes)) {
            is Result.Success -> {
                val domain = net.data.toDomain()
                local.cacheRecipes(listOf(domain))
                Result.Success(domain)
            }
            is Result.Error -> Result.Error(net.error)
        }
    }

    override suspend fun getMyRecipes(): Result<List<Recipe>> {
        when (val net = remote.myRecipes()) {
            is Result.Success -> {
                val domain = net.data.map { it.toDomain() }
                local.cacheRecipes(domain)
                return Result.Success(domain)
            }
            is Result.Error -> {
                val cache = local.getAllRecipes() // or filter by ownerId if stored
                return if (cache is Result.Success && cache.data.isNotEmpty()) cache
                else Result.Error(net.error)
            }
        }
    }

    override suspend fun addFavorite(id: String): Result<Recipe> {
        return when (val net = remote.addFavorite(id)) {
            is Result.Success -> {
                val domain = net.data.toDomain()
                local.cacheRecipes(listOf(domain))
                Result.Success(domain)
            }
            is Result.Error -> Result.Error(net.error)
        }
    }

    override suspend fun removeFavorite(id: String): Result<Recipe> {
        return when (val net = remote.removeFavorite(id)) {
            is Result.Success -> {
                val domain = net.data.toDomain()
                local.cacheRecipes(listOf(domain))
                Result.Success(domain)
            }
            is Result.Error -> Result.Error(net.error)
        }
    }

    override suspend fun getMyFavorites(): Result<List<Recipe>> {
        when (val net = remote.myFavorites()) {
            is Result.Success -> {
                val domain = net.data.map { it.toDomain() }
                // optional: cache
                return Result.Success(domain)
            }
            is Result.Error -> {
                // fallback: empty or local search
                return Result.Error(net.error)
            }
        }
    }

    // --- mapping helpers ---
    private fun toCreateRequest(recipe: Recipe): CreateRecipeRequest =
        CreateRecipeRequest(
            title = recipe.title,
            instructions = recipe.instructions,
            ownerId = recipe.ownerId,
            ingredients = recipe.ingredients.map { IngredientRequest(name = it.name, measure = it.measure) },
            youtubeUrl = recipe.youtubeUrl ?: ""
        )

    private fun toUpdateRequest(recipe: Recipe): UpdateRecipeRequest =
        UpdateRecipeRequest(
            title = recipe.title,
            instructions = recipe.instructions,
            ingredients = recipe.ingredients.map { IngredientRequest(name = it.name, measure = it.measure) },
            youtubeUrl = recipe.youtubeUrl ?: ""
        )
}
