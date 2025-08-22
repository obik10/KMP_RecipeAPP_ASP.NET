package org.robiul.kmprecipeapp.domain.usecase

import org.robiul.kmprecipeapp.data.repository.RecipeRepositoryImpl

class GetRecipeById(private val repo: RecipeRepositoryImpl) {
    suspend operator fun invoke(id: String) = repo.getRecipeById(id)
}
