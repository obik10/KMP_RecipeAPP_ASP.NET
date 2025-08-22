package org.robiul.kmprecipeapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.robiul.kmprecipeapp.ui.editor.AddEditRecipeScreen
import org.robiul.kmprecipeapp.ui.favorites.FavoritesScreen
import org.robiul.kmprecipeapp.ui.login.LoginScreen
import org.robiul.kmprecipeapp.ui.mine.MyRecipesScreen
import org.robiul.kmprecipeapp.ui.register.RegisterScreen
import org.robiul.kmprecipeapp.ui.recipes.RecipeDetailScreen
import org.robiul.kmprecipeapp.ui.recipes.RecipeListScreen

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Recipes = "recipes"
    const val Recipe = "recipe/{id}"
    const val Add = "add"
    const val Edit = "recipe/{id}/edit"
    const val MyRecipes = "my-recipes"
    const val Favorites = "favorites"
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    startDestination: String = Routes.Login,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Routes.Login) {
            LoginScreen(onSuccess = {
                navController.navigate(Routes.Recipes) {
                    popUpTo(Routes.Login) { inclusive = true }
                }
            }, onRegister = { navController.navigate(Routes.Register) })
        }

        composable(Routes.Register) {
            RegisterScreen(onRegisterSuccess = {
                navController.navigate(Routes.Recipes) {
                    popUpTo(Routes.Login) { inclusive = true }
                }
            }, onCancel = { navController.popBackStack() })
        }

        composable(Routes.Recipes) {
            RecipeListScreen(onOpen = { id -> navController.navigate("recipe/$id") }, onAdd = { navController.navigate(Routes.Add) }, onFavorites = { navController.navigate(Routes.Favorites) }, onMyRecipes = { navController.navigate(Routes.MyRecipes) })
        }

        composable(Routes.Favorites) {
            FavoritesScreen(onOpen = { id -> navController.navigate("recipe/$id") }, onBack = { navController.popBackStack() })
        }

        composable("recipe/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            RecipeDetailScreen(id = id, onEdit = { navController.navigate("recipe/$id/edit") }, onBack = { navController.popBackStack() })
        }

        composable(Routes.Add) {
            AddEditRecipeScreen(mode = "add", onSaved = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
        }

        composable("recipe/{id}/edit") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            AddEditRecipeScreen(mode = "edit", id = id, onSaved = { navController.popBackStack() }, onCancel = { navController.popBackStack() })
        }

        composable(Routes.MyRecipes) {
            MyRecipesScreen(onOpen = { id -> navController.navigate("recipe/$id") }, onBack = { navController.popBackStack() })
        }
    }
}