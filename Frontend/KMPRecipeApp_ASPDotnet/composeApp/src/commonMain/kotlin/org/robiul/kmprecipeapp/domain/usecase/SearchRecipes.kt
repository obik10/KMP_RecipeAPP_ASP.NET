package org.robiul.kmprecipeapp.domain.usecase

import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.utils.Result

class SearchRecipes(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(keyword: String): Result<List<Recipe>> {
        return repository.searchRecipes(keyword)
    }
}
