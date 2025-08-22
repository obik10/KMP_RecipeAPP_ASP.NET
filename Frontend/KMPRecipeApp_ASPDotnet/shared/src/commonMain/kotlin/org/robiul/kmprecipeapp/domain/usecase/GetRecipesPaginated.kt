package org.robiul.kmprecipeapp.domain.usecase

import org.robiul.kmprecipeapp.domain.repository.RecipeRepository

class GetRecipesPaginated(private val repo: RecipeRepository) {
    suspend operator fun invoke(pageNumber: Int, pageSize: Int) = repo.getRecipes(pageNumber, pageSize)
}
