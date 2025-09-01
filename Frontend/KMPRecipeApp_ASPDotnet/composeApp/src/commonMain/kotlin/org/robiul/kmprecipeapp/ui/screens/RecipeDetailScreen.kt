package org.robiul.kmprecipeapp.ui.screens

import RecipeDetailState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.robiul.kmprecipeapp.presentation.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    state: RecipeDetailState,
    onRetry: () -> Unit,
    favoritesViewModel: FavoritesViewModel
) {
    val uriHandler = LocalUriHandler.current
    val favoriteIds by favoritesViewModel.favoriteIds.collectAsState()

    when {
        state.isLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        state.errorMessage != null -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Error: ${state.errorMessage}",
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error)
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRetry, shape = RoundedCornerShape(50)) {
                Text("Retry")
            }
        }

        state.recipe != null -> {
            val recipe = state.recipe
            val isFav = favoriteIds.contains(recipe.id)

            Box(modifier = Modifier.fillMaxSize()) {
                // Content (scrollable)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // --- Hero Image ---
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(24.dp)) // same as RecipeCard
                                .shadow(8.dp, RoundedCornerShape(24.dp))
                        ) {
                            if (!recipe.imagePath.isNullOrBlank()) {
                                AsyncImage(
                                    model = recipe.imagePath,
                                    contentDescription = recipe.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                                startY = 200f
                                            )
                                        )
                                )
                                Text(
                                    recipe.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                )
                            }
                        }
                    }

                    // --- Instructions ---
                    item {
                        Column {
                            Text(
                                "Instructions",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                recipe.instructions,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // --- Ingredients ---
                    item {
                        Column {
                            Text(
                                "Ingredients",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    items(recipe.ingredients) { ingredient ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${ingredient.name} (${ingredient.measure})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // --- Floating pill bottom bar ---
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .shadow(12.dp, RoundedCornerShape(50))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!recipe.youtubeUrl.isNullOrBlank()) {
                        Button(
                            onClick = { uriHandler.openUri(recipe.youtubeUrl!!) },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            Spacer(Modifier.width(6.dp))
                            Text("Watch Video")
                        }
                    }

                    IconButton(
                        onClick = { favoritesViewModel.toggleFavorite(recipe) },
                        modifier = Modifier
                            .size(25.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFav) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFav) Color.Red
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
