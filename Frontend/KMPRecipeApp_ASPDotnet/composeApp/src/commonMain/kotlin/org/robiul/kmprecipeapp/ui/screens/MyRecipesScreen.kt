package org.robiul.kmprecipeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.robiul.kmprecipeapp.presentation.model.RecipeUiModel
import org.robiul.kmprecipeapp.presentation.viewmodel.FavoritesViewModel
import org.robiul.kmprecipeapp.presentation.viewmodel.MyRecipesViewModel
import org.robiul.kmprecipeapp.ui.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecipesScreen(
    myRecipesViewModel: MyRecipesViewModel,
    favoritesViewModel: FavoritesViewModel,
    onRecipeClick: (RecipeUiModel) -> Unit,
    onEditClick: (RecipeUiModel) -> Unit,
    onAddClick: () -> Unit // ✅ new callback
) {
    val state = myRecipesViewModel.state

    LaunchedEffect(Unit) {
        myRecipesViewModel.loadMyRecipes()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.errorMessage != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Error: ${state.errorMessage}") }

            state.recipes.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("You haven't added any recipes yet") }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                ) {
                    items(state.recipes, key = { it.id }) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            favoritesViewModel = favoritesViewModel,
                            isMyRecipe = true,
                            onClick = { onRecipeClick(recipe) },
                            onEditClick = { onEditClick(recipe) }
                        )
                    }
                }
            }
        }

        // ✅ Floating Button
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Recipe")
        }
    }
}
