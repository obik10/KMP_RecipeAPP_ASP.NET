package org.robiul.kmprecipeapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar(currentRoute: String?, onNavigate: (route: String) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = currentRoute?.startsWith("recipes") == true,
            onClick = { onNavigate("recipes") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = null
        )

        NavigationBarItem(
            selected = currentRoute == "favorites",
            onClick = { onNavigate("favorites") },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
            label = null
        )

        NavigationBarItem(
            selected = currentRoute == "my-recipes",
            onClick = { onNavigate("my-recipes") },
            icon = { Icon(Icons.Default.Create, contentDescription = "My Recipes") },
            label = null
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = null
        )
    }
}
