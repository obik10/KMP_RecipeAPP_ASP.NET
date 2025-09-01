package org.robiul.kmprecipeapp.domain.usecase

import org.robiul.kmprecipeapp.domain.models.PaginatedResult
import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.utils.Result

class GetRecipesPaginated(private val repo: RecipeRepository) {
    suspend operator fun invoke(pageNumber: Int, pageSize: Int): Result<PaginatedResult<Recipe>> =
        repo.getRecipes(pageNumber, pageSize)
}
