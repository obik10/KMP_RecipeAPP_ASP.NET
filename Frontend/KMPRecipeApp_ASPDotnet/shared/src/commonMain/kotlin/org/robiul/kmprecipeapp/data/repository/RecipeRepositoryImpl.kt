package org.robiul.kmprecipeapp.data.repository

import kotlinx.serialization.json.Json
import org.robiul.kmprecipeapp.data.datasource.LocalDataSource
import org.robiul.kmprecipeapp.data.datasource.RemoteDataSource
import org.robiul.kmprecipeapp.data.models.dto.CreateRecipeRequest
import org.robiul.kmprecipeapp.data.models.dto.IngredientRequest
import org.robiul.kmprecipeapp.data.models.dto.UpdateRecipeRequest
import org.robiul.kmprecipeapp.data.models.toDomain
import org.robiul.kmprecipeapp.domain.models.Ingredient
import org.robiul.kmprecipeapp.domain.models.PaginatedResult
import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.utils.Result

class RecipeRepositoryImpl(
    private val remote: RemoteDataSource,
    private val local: LocalDataSource
) : RecipeRepository {

    override suspend fun getRecipes(pageNumber: Int, pageSize: Int): Result<PaginatedResult<Recipe>> {
        return when (val net = remote.listPaginated(pageNumber, pageSize)) {
            is Result.Success -> {
                val domainItems = net.data.items.map { it.toDomain() }
                local.cacheRecipes(domainItems)
                Result.Success(
                    PaginatedResult(
                        items = domainItems,
                        totalCount = net.data.totalCount,
                        pageNumber = net.data.pageNumber,
                        pageSize = net.data.pageSize,
                        totalPages = net.data.totalPages
                    )
                )
            }
            is Result.Error -> {
                val cache = local.getAllRecipes()
                if (cache is Result.Success && cache.data.isNotEmpty()) {
                    Result.Success(
                        PaginatedResult(
                            items = cache.data,
                            totalCount = cache.data.size,
                            pageNumber = pageNumber,
                            pageSize = pageSize,
                            totalPages = null
                        )
                    )
                } else {
                    Result.Error(net.error)
                }
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
    private val json = Json { prettyPrint = true }


    override suspend fun createRecipe(recipe: Recipe): Result<Recipe> {
        val req = toCreateRequest(recipe)
        // debug
        println("▶︎ createRecipe JSON:\n${json.encodeToString(req)}")

        println("▶︎ createRecipe request: title='${req.title}', ingredients=${req.ingredients.map { it.name to it.measure }}")
        return when (val net = remote.create(req)) {
            is Result.Success -> {
                val domain = net.data.toDomain()
                // optionally cache created recipe
                local.cacheRecipes(listOf(domain))
                Result.Success(domain)
            }
            is Result.Error -> {
                println("❌ createRecipe failed: ${net.error}")
                Result.Error(net.error)
            }
        }
    }

    override suspend fun updateRecipe(id: String, recipe: Recipe): Result<Recipe> {
        val req = toUpdateRequest(recipe)

        println("▶︎ updateRecipe JSON:\n${json.encodeToString(req)}")
        println("📤 Sending UpdateRecipeRequest JSON: ${json.encodeToString(UpdateRecipeRequest.serializer(), req)}")

        // debug
        println("▶︎ updateRecipe id=$id request: title='${req.title}', ingredients=${req.ingredients.map { it.name to it.measure }}")
        return when (val net = remote.update(id, req)) {
            is Result.Success -> {
                val domain = net.data.toDomain()
                local.cacheRecipes(listOf(domain))
                Result.Success(domain)
            }
            is Result.Error -> {
                println("❌ updateRecipe failed for id=$id: ${net.error}")
                Result.Error(net.error)
            }
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
        println("⚠️ removeFavorite called with id=$id")
        val net = remote.removeFavorite(id)
        println("⚠️ removeFavorite network result=$net")
        return when (net) {
            is Result.Success -> {
                println("✅ removeFavorite SUCCESS for id=$id")
                val domain = net.data.toDomain()
                local.cacheRecipes(listOf(domain))
                Result.Success(domain)
            }
            is Result.Error -> {
                println("❌ removeFavorite FAILED for id=$id, error=${net.error}")
                Result.Error(net.error)
            }
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
    private fun normalizeIngredient(i: Ingredient): IngredientRequest =
        IngredientRequest(
            name = i.name.trim(),
            measure = i.measure.trim()
        )

    private fun toCreateRequest(recipe: Recipe): CreateRecipeRequest =
        CreateRecipeRequest(
            title = recipe.title.trim(),
            instructions = recipe.instructions.trim(),
            ownerId = recipe.ownerId,
            ingredients = recipe.ingredients
                .map { normalizeIngredient(it) }
                .filter { it.name.isNotBlank() }, // drop empty rows
            youtubeUrl = recipe.youtubeUrl?.trim() ?: ""
        )

    private fun toUpdateRequest(recipe: Recipe): UpdateRecipeRequest =
        UpdateRecipeRequest(
            title = recipe.title.trim(),
            instructions = recipe.instructions.trim(),
            ingredients = recipe.ingredients
                .map { normalizeIngredient(it) }
                .filter { it.name.isNotBlank() || it.measure.isNotBlank() }, // keep if either filled
            youtubeUrl = recipe.youtubeUrl?.trim() ?: ""
        )


}
