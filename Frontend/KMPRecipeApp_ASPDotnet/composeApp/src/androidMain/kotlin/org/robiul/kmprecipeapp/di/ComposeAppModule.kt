package org.robiul.kmprecipeapp.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.robiul.kmprecipeapp.viewmodel.AddEditRecipeViewModel
import org.robiul.kmprecipeapp.viewmodel.FavoritesViewModel
import org.robiul.kmprecipeapp.viewmodel.LoginViewModel
import org.robiul.kmprecipeapp.viewmodel.MyRecipesViewModel
import org.robiul.kmprecipeapp.viewmodel.RecipeDetailViewModel
import org.robiul.kmprecipeapp.viewmodel.RecipeListViewModel

val composeAppModule = module {
    viewModel { LoginViewModel() }
    viewModel { RecipeListViewModel() }
    viewModel { RecipeDetailViewModel() }
    viewModel { AddEditRecipeViewModel() }
    viewModel { MyRecipesViewModel() }
    viewModel { FavoritesViewModel() }
}
