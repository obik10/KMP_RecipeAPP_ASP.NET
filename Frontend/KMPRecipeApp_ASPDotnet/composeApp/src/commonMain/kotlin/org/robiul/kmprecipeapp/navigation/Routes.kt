package org.robiul.kmprecipeapp.navigation

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Recipes = "recipes"
    const val Add = "add"
    const val Recipe = "recipe"          // route base, full route will be "recipe/{id}"
    const val RecipeWithId = "recipe/{id}"
    const val Edit = "recipe/{id}/edit"
    const val MyRecipes = "my-recipes"
    const val Favorites = "favorites"
    const val Profile = "profile"
}

/**
 * Protected route *prefixes* (used to check protection quickly).
 * For a route like "recipe/{id}" we check prefix "recipe" when guarding.
 */
val protectedRoutePrefixes = setOf("add", "recipe", "my-recipes", "favorites")
