package org.robiul.kmprecipeapp.domain.usecase

import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.utils.Result

class GetMyRecipes(private val repo: RecipeRepository) {
    suspend operator fun invoke() = repo.getMyRecipes()
}
