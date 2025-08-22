package org.robiul.kmprecipeapp.domain.usecase

import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.utils.Result

class RemoveFavorite(private val repo: RecipeRepository) {
    suspend operator fun invoke(id: String): Result<Recipe> = repo.removeFavorite(id)
}
