package org.robiul.kmprecipeapp.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.robiul.kmprecipeapp.Constants
import org.robiul.kmprecipeapp.data.repository.AuthRepositoryImpl
import org.robiul.kmprecipeapp.domain.repository.AuthRepository
import org.robiul.kmprecipeapp.viewmodel.AddEditRecipeViewModel
import org.robiul.kmprecipeapp.viewmodel.AuthViewModel
import org.robiul.kmprecipeapp.viewmodel.FavoritesViewModel
import org.robiul.kmprecipeapp.viewmodel.LoginViewModel
import org.robiul.kmprecipeapp.viewmodel.MyRecipesViewModel
import org.robiul.kmprecipeapp.viewmodel.RecipeDetailViewModel
import org.robiul.kmprecipeapp.viewmodel.RecipeListViewModel

val composeAppModule = module {
    // Register all ViewModels
    viewModel { AuthViewModel() }
    viewModel { LoginViewModel() }
    viewModel { RecipeListViewModel() }
    viewModel { RecipeDetailViewModel() }
    viewModel { AddEditRecipeViewModel() }
    viewModel { MyRecipesViewModel() }
    viewModel { FavoritesViewModel() }


    single<String> { Constants.BASE_URL_KEYCLOAK } // swap by buildType

    single<AuthRepository> {
        AuthRepositoryImpl(
            remote = get(),
            tokenStore = get(),
            keycloakUrl = get()
        )
    }

}
