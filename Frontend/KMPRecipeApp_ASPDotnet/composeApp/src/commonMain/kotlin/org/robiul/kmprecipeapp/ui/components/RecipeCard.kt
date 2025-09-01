package org.robiul.kmprecipeapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.robiul.kmprecipeapp.presentation.model.RecipeUiModel
import org.robiul.kmprecipeapp.presentation.model.toDomain
import org.robiul.kmprecipeapp.presentation.viewmodel.FavoritesViewModel

@Composable
fun RecipeCard(
    recipe: RecipeUiModel,
    favoritesViewModel: FavoritesViewModel? = null,
    isMyRecipe: Boolean = false,
    onClick: () -> Unit = {},
    onEditClick: (() -> Unit)? = null
) {
    val isFavorite = if (!isMyRecipe && favoritesViewModel != null) {
        val favoriteIds by favoritesViewModel.favoriteIds.collectAsState()
        favoriteIds.contains(recipe.id)
    } else false

    // --- Animation state ---
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 500),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .clickable { onClick() }
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            // --- Recipe Image ---
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // --- Gradient overlay ---
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f)
                            ),
                            startY = 120f
                        )
                    )
            )

            // --- Title ---
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )

            // --- Action Button ---
            IconButton(
                onClick = {
                    if (isMyRecipe) {
                        onEditClick?.invoke()
                    } else {
                        favoritesViewModel?.toggleFavorite(recipe.toDomain())
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .shadow(8.dp, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .size(44.dp)
            ) {
                if (isMyRecipe) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit recipe",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    if (isFavorite) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Remove from favorites",
                            tint = Color.Red
                        )
                    } else {
                        Icon(
                            Icons.Outlined.FavoriteBorder,
                            contentDescription = "Add to favorites",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
