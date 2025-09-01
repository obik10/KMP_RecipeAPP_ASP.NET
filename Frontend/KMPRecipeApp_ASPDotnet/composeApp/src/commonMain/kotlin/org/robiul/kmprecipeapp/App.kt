package org.robiul.kmprecipeapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import org.robiul.kmprecipeapp.ui.BottomNavBar
import org.robiul.kmprecipeapp.navigation.NavGraph
import org.robiul.kmprecipeapp.di.AppModule
import org.robiul.kmprecipeapp.navigation.LocalFavoritesViewModel
import org.robiul.kmprecipeapp.navigation.LocalProfileViewModel

@Composable
fun App() {
    MaterialTheme {
        val driver = rememberDatabaseDriverFactory().createDriver()

        remember(driver) {
            AppModule.init(driver)
        }

        // ✅ Provide global VMs
        val profileViewModel = remember { AppModule.provideProfileViewModel() }
        val favoritesViewModel = remember { AppModule.provideFavoritesViewModel() }

        LaunchedEffect(profileViewModel.isLoggedIn) {
            profileViewModel.loadUser()
            if (profileViewModel.isLoggedIn) {
                favoritesViewModel.loadFavorites()
            } else {
                favoritesViewModel.clearFavorites()
            }
        }

        CompositionLocalProvider(
            LocalProfileViewModel provides profileViewModel,
            LocalFavoritesViewModel provides favoritesViewModel
        ) {
            Navigator(screen = NavGraph.Home) { navigator ->
                Column {
                    Box(modifier = Modifier.weight(1f)) {
                        navigator.lastItem.Content()
                    }

                    BottomNavBar(
                        currentScreen = navigator.lastItem as NavGraph,
                        onNavigate = { destination -> navigator.push(destination) },
                        isLoggedIn = profileViewModel.isLoggedIn
                    )
                }
            }
        }
    }
}



