package org.robiul.kmprecipeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import org.robiul.kmprecipeapp.presentation.model.RecipeUiModel
import org.robiul.kmprecipeapp.presentation.mapper.toUiModel
import org.robiul.kmprecipeapp.presentation.viewmodel.FavoritesViewModel
import org.robiul.kmprecipeapp.ui.components.RecipeCard

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onRecipeClick: (RecipeUiModel) -> Unit
) {
    val state = viewModel.state

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.errorMessage != null -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error: ${state.errorMessage}")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.loadFavorites() }) {
                    Text("Retry")
                }
            }
            state.recipes.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No favorites yet")
            }
            else ->LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.recipes, key = { it.id }) { recipeDomain ->
                    val recipeUi = recipeDomain.toUiModel()
                    RecipeCard(
                        recipe = recipeUi,
                        favoritesViewModel = viewModel,
                        onClick = { onRecipeClick(recipeUi) }
                    )
                }
            }

        }
    }
}
