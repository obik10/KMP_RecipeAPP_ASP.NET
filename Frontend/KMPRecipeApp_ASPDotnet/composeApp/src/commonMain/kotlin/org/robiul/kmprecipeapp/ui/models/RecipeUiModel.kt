//// RecipeUiModel.kt
//package org.robiul.kmprecipeapp.ui.models
//
//import org.robiul.kmprecipeapp.domain.models.Recipe
//
//data class RecipeUiModel(
//    val id: String,
//    val title: String,
//    val displayImageUrl: String
//) {
//    companion object {
//        fun from(recipe: Recipe) = RecipeUiModel(
//            id = recipe.id ?: recipe.title.hashCode().toString(),
//            title = recipe.title,
//            displayImageUrl = recipe.imagePath.orEmpty()
//        )
//    }
//}
