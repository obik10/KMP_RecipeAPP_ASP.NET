package org.robiul.kmprecipeapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel
import org.robiul.kmprecipeapp.ui.login.LoginScreen
import org.robiul.kmprecipeapp.ui.profile.ProfileScreen
import org.robiul.kmprecipeapp.ui.recipes.RecipeListScreen
import org.robiul.kmprecipeapp.viewmodel.AuthViewModel
import org.robiul.kmprecipeapp.ui.components.BottomNavBar
import org.robiul.kmprecipeapp.ui.login.RegisterScreen

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Recipes = "recipes"
    const val Add = "add"
    const val Recipe = "recipe/{id}"
    const val Edit = "recipe/{id}/edit"
    const val MyRecipes = "my-recipes"
    const val Favorites = "favorites"
    const val Profile = "profile"
}

private val protectedRoutes = setOf(Routes.Add, Routes.Edit, Routes.MyRecipes, Routes.Favorites)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(startDestination: String = Routes.Recipes) {
    val navController = rememberNavController()
    val authVm: AuthViewModel = koinViewModel()
    val tokens by authVm.tokens.collectAsState()

    // Observe tokens and if user logs out while on a protected route, navigate to Recipes
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(tokens, currentRoute) {
        if (tokens == null && currentRoute != null && protectedRoutes.any { currentRoute.startsWith(it.removeSuffix("/{id}")) }) {
            navController.navigate(Routes.Recipes) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    // If route is protected and user not logged in, take them to login
                    if (protectedRoutes.contains(route) && tokens == null) {
                        navController.navigate(Routes.Login) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Login) {
                LoginScreen(onSuccess = {
                    // On success we navigate to Recipes; nav handled by LoginScreen callback
                    navController.navigate(Routes.Recipes) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                })
            }

            composable(Routes.Register) {
                RegisterScreen(onSuccess = {
                    // After registration, go straight to Recipes
                    navController.navigate(Routes.Recipes) {
                        popUpTo(Routes.Register) { inclusive = true }
                    }
                })
            }

            composable(Routes.Recipes) {
                RecipeListScreen(onOpen = { id -> navController.navigate("recipe/$id") })
            }

            composable(Routes.Favorites) {
                // TODO: FavoritesScreen
            }

            composable(Routes.Add) {
                // TODO: AddEditRecipeScreen(mode = "add")
            }

            composable(Routes.Recipe) { back ->
                val id = back.arguments?.getString("id") ?: ""
                // TODO: RecipeDetailScreen(id = id)
            }

            composable(Routes.MyRecipes) {
                // TODO: MyRecipesScreen(onOpen = { id -> navController.navigate("recipe/$id") })
            }

            composable(Routes.Profile) {
                ProfileScreen(onNavigateToLogin = { navController.navigate(Routes.Login) }, onNavigateToRegister = { navController.navigate(Routes.Register) })
            }
        }
    }
}
