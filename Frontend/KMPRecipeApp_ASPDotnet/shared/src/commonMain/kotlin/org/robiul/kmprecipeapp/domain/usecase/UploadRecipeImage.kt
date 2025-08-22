package org.robiul.kmprecipeapp.domain.usecase

import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.utils.Result

class UploadRecipeImage(private val repo: RecipeRepository) {
    suspend operator fun invoke(id: String, fileName: String, bytes: ByteArray): Result<Recipe> =
        repo.uploadRecipeImage(id, fileName, bytes)
}
