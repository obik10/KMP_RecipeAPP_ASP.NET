package org.robiul.kmprecipeapp.ui.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecipeListScreen(
    onOpen: (String) -> Unit,
    onAdd: () -> Unit = {},
    onFavorites: () -> Unit = {},
    onMyRecipes: () -> Unit = {}
) {
    val items = remember { List(10) { "recipe-${it + 1}" } }

    Scaffold(snackbarHost = { SnackbarHost(hostState = SnackbarHostState()) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(items) { id ->
                Column(
                    modifier = Modifier
                        .clickable { onOpen(id) }
                        .padding(16.dp)
                ) {
                    Text(text = "Recipe $id")
                    Text(text = "Short description...", modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
