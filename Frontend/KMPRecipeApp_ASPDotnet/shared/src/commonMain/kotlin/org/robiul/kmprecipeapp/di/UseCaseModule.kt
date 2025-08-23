package org.robiul.kmprecipeapp.di

import org.koin.dsl.module
import org.robiul.kmprecipeapp.domain.usecase.*

val useCaseModule = module {
    single { RegisterUser(get()) }
    single { GetRecipesPaginated(get()) }
    single { SearchRecipes(get()) }
    single { GetRecipeById(get()) }
    single { CreateRecipe(get()) }
    single { UpdateRecipe(get()) }
    single { DeleteRecipe(get()) }
    single { UploadRecipeImage(get()) }
    single { GetMyRecipes(get()) }
    single { AddFavorite(get()) }
    single { RemoveFavorite(get()) }
    single { GetMyFavorites(get()) }
}
