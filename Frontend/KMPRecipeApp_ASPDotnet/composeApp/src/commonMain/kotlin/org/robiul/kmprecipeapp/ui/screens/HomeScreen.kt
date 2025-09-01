package org.robiul.kmprecipeapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.robiul.kmprecipeapp.presentation.model.RecipeUiModel
import org.robiul.kmprecipeapp.presentation.state.HomeUiState
import org.robiul.kmprecipeapp.presentation.viewmodel.FavoritesViewModel
import org.robiul.kmprecipeapp.ui.components.RecipeCard
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRecipeClick: (RecipeUiModel) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onLoadPage: (page: Int) -> Unit,
    favoritesViewModel: FavoritesViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val favoriteIds by favoritesViewModel.favoriteIds.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Discover Recipes", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
            ) {
                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search recipes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = MaterialTheme.shapes.large,
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                // Content
                when {
                    state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    state.errorMessage != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Error: ${state.errorMessage}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                    state.recipes.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No recipes found",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.recipes, key = { it.id }) { recipeUi ->
                                RecipeCard(
                                    recipe = recipeUi,
                                    favoritesViewModel = favoritesViewModel,
                                    onClick = { onRecipeClick(recipeUi) }
                                )
                            }
                        }
                    }
                }
            }

            // Floating PaginationBar (bottom center, over the grid)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                PaginationBar(
                    currentPage = state.currentPage,
                    totalPages = state.totalPages,
                    onLoadPage = onLoadPage,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .shadow(8.dp, RoundedCornerShape(50))
                )
            }
        }
    }
}



@Composable
fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    onLoadPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalPages > 1) {
        Row(
            modifier = modifier
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .wrapContentWidth()
                .height(35.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { if (currentPage > 1) onLoadPage(currentPage - 1) },
                enabled = currentPage > 1
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
            }

            Text(
                text = "Page $currentPage / $totalPages",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )

            IconButton(
                onClick = { if (currentPage < totalPages) onLoadPage(currentPage + 1) },
                enabled = currentPage < totalPages
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

