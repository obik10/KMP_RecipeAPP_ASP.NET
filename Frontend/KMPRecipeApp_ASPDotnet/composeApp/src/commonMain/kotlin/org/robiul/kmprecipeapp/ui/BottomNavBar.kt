package org.robiul.kmprecipeapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.robiul.kmprecipeapp.navigation.NavGraph

@Composable
fun BottomNavBar(
    currentScreen: NavGraph,
    onNavigate: (NavGraph) -> Unit,
    isLoggedIn: Boolean
) {
    val items = listOf(
        NavGraph.Home to Icons.Default.Home,
        NavGraph.Favorites to Icons.Default.Favorite,
        NavGraph.MyRecipes to Icons.Default.Restaurant,
        NavGraph.Profile to Icons.Default.Person
    )

    NavigationBar(
        tonalElevation = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )
                )
            )
    ) {
        items.forEach { (screen, icon) ->
            val isSelected = currentScreen::class == screen::class

            NavigationBarItem(
                icon = {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            )
                        }
                        Icon(
                            icon,
                            contentDescription = screen::class.simpleName,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else LocalContentColor.current.copy(alpha = 0.8f)
                        )
                    }
                },
                label = {
                    Text(
                        screen::class.simpleName ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else LocalContentColor.current.copy(alpha = 0.8f)
                    )
                },
                selected = isSelected,
                onClick = {
                    if ((screen is NavGraph.Favorites || screen is NavGraph.MyRecipes) && !isLoggedIn) {
                        onNavigate(NavGraph.Login)
                    } else {
                        onNavigate(screen)
                    }
                }
            )
        }
    }
}
