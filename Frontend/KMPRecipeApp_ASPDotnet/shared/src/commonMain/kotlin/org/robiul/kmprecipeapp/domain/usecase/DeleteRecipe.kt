package org.robiul.kmprecipeapp.domain.usecase

import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.utils.Result

class DeleteRecipe(private val repo: RecipeRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repo.deleteRecipe(id)
}
