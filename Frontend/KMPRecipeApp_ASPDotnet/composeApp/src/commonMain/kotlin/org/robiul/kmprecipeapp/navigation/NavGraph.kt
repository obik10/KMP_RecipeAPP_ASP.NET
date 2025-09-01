package org.robiul.kmprecipeapp.navigation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.Uri
import org.robiul.kmprecipeapp.core.PickImageLauncher
import org.robiul.kmprecipeapp.di.AppModule
import org.robiul.kmprecipeapp.domain.usecase.GetRecipeById
import org.robiul.kmprecipeapp.presentation.viewmodel.*
import org.robiul.kmprecipeapp.rememberDatabaseDriverFactory
import org.robiul.kmprecipeapp.ui.screens.*

/** CompositionLocals for global ViewModels **/
val LocalProfileViewModel = compositionLocalOf<ProfileViewModel> { error("ProfileViewModel not provided") }
val LocalFavoritesViewModel = compositionLocalOf<FavoritesViewModel> { error("FavoritesViewModel not provided") }

sealed class NavGraph : Screen {

    // ---------------- HOME ----------------
    object Home : NavGraph() {
        @Composable
        override fun Content() {
            val viewModel = remember { AppModule.provideHomeViewModel() }
            val favoritesViewModel = LocalFavoritesViewModel.current
            val state = viewModel.state
            val navigator = cafe.adriel.voyager.navigator.LocalNavigator.currentOrThrow

            LaunchedEffect(Unit) {
                viewModel.loadPage(1)
                favoritesViewModel.loadFavorites()
            }

            when {
                state.isLoading && state.recipes.isEmpty() -> CircularProgressIndicator()
                state.errorMessage != null -> Text("Error: ${state.errorMessage}")
                else -> HomeScreen(
                    state = state,
                    onRecipeClick = { navigator.push(RecipeDetail(it.id)) },
                    onSearchQueryChange = { query -> viewModel.search(query) },
                    onLoadPage = { page -> viewModel.loadPage(page) },
                    favoritesViewModel = favoritesViewModel
                )
            }
        }
    }

    // ---------------- RECIPE DETAIL ----------------
    data class RecipeDetail(val recipeId: String) : NavGraph() {
        @Composable
        override fun Content() {
            val driver = rememberDatabaseDriverFactory().createDriver()

            // shared repository + use case
            val repository = remember { AppModule.provideRecipeRepository() }
            val getRecipeById = remember { GetRecipeById(repository) }
            val viewModel = remember { RecipeDetailViewModel(getRecipeById) }
            val favoritesViewModel = LocalFavoritesViewModel.current // <- pass this into the screen
            val state = viewModel.state

            LaunchedEffect(recipeId) {
                viewModel.loadRecipe(recipeId)
            }

            // Pass favoritesViewModel so detail screen can use the global favorites state
            RecipeDetailScreen(
                state = state,
                onRetry = { viewModel.loadRecipe(recipeId) },
                favoritesViewModel = favoritesViewModel
            )
        }
    }

    // ---------------- PROFILE ----------------
    object Profile : NavGraph() {
        @Composable
        override fun Content() {
            val navigator = cafe.adriel.voyager.navigator.LocalNavigator.currentOrThrow
            val viewModel = LocalProfileViewModel.current

            LaunchedEffect(Unit) { viewModel.loadUser() }

            ProfileScreen(
                isLoggedIn = viewModel.isLoggedIn,
                onLogout = { viewModel.logout() },
                onLoginClick = { navigator.push(Login) },
                onRegisterClick = { navigator.push(Register) }
            )
        }
    }

    // ---------------- LOGIN ----------------
    object Login : NavGraph() {
        @Composable
        override fun Content() {
            val navigator = cafe.adriel.voyager.navigator.LocalNavigator.currentOrThrow
            val viewModel = remember { AppModule.provideLoginViewModel() }
            val profileViewModel = LocalProfileViewModel.current

            LoginScreen(
                onLogin = { email, password ->
                    viewModel.login(email, password) {
                        profileViewModel.loadUser()
                        navigator.pop()
                    }
                },
                onNavigateToRegister = { navigator.push(Register) },
                isLoading = viewModel.isLoading,
                errorMessage = viewModel.errorMessage
            )
        }
    }

    // ---------------- REGISTER ----------------
    object Register : NavGraph() {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            val viewModel = remember { AppModule.provideRegisterViewModel() }

            RegisterScreen(
                onRegister = { name, email, password ->
                    viewModel.register(name, email, password) { navigator.pop() }
                },
                onNavigateToLogin = { navigator.push(Login) },
                isLoading = viewModel.isLoading,
                errorMessage = viewModel.errorMessage
            )
        }
    }

    // ---------------- FAVORITES ----------------
    object Favorites : NavGraph() {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            val viewModel = LocalFavoritesViewModel.current

            FavoritesScreen(
                viewModel = viewModel,
                onRecipeClick = { recipeUi ->
                    navigator.push(RecipeDetail(recipeUi.id))
                }
            )
        }
    }

    // ---------------- MY RECIPES ----------------
    object MyRecipes : NavGraph() {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow

            // Get ViewModels
            val myRecipesViewModel = remember { AppModule.provideMyRecipesViewModel() }
            val favoritesViewModel = LocalFavoritesViewModel.current

            // Pass ViewModels directly to screen
            MyRecipesScreen(
                myRecipesViewModel = myRecipesViewModel,
                favoritesViewModel = favoritesViewModel,
                onRecipeClick = { recipeUi ->
                    // open recipe detail
                    navigator.push(RecipeDetail(recipeUi.id))
                },
                onEditClick = { recipeUi ->
                    // navigate to Edit screen
                    navigator.push(EditMyRecipe(recipeUi.id))
                },
                onAddClick = { navigator.push(AddRecipeNav) } // ✅ go to add screen

            )
        }
    }

    // ---------------- EDIT MY RECIPE ----------------
    data class EditMyRecipe(val recipeId: String) : NavGraph() {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            val repository = remember { AppModule.provideRecipeRepository() }
            val viewModel = remember { MyRecipesEditViewModel(repository, recipeId) }

            MyRecipesEditScreen(
                viewModel = viewModel,
                onBack = { navigator.pop() }
            )
        }
    }

    // ---------------- ADD NEW RECIPE ----------------
    object AddRecipeNav : NavGraph() {
        @Composable
        override fun Content() {
            val navigator = LocalNavigator.currentOrThrow
            val repository = remember { AppModule.provideRecipeRepository() }
            val viewModel = remember { MyRecipesAddViewModel(repository) }

            MyRecipesAddScreen(
                viewModel = viewModel,
                onBack = { navigator.pop() }
            )
        }
    }

}