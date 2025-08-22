package org.robiul.kmprecipeapp.domain.usecase

import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.utils.Result

class GetMyFavorites(private val repo: RecipeRepository) {
    suspend operator fun invoke(): Result<List<org.robiul.kmprecipeapp.domain.models.Recipe>> =
        repo.getMyFavorites()
}
