package org.robiul.kmprecipeapp.domain.usecase

import org.robiul.kmprecipeapp.domain.repository.RecipeRepository

class SearchRecipes(private val repo: RecipeRepository) {
    suspend operator fun invoke(keyword: String) = repo.searchRecipes(keyword)
}
