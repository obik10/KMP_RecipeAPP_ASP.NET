package org.robiul.kmprecipeapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.robiul.kmprecipeapp.domain.models.Recipe
import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.presentation.state.FavoritesUiState
import org.robiul.kmprecipeapp.utils.AppError
import org.robiul.kmprecipeapp.utils.Result

class FavoritesViewModel(
    private val repository: RecipeRepository,
    private val settings: Settings
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val FAVORITES_KEY = "favorites_ids"

    // Reactive set of favorite IDs
    private val _favoriteIds = MutableStateFlow(loadCachedIds())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    // Compose-friendly UI state (list of Recipe details; used by Favorites screen)
    var state by mutableStateOf(FavoritesUiState())
        private set

    init {
        // attempt initial load
        loadFavorites()
    }

    fun loadFavorites() {
        scope.launch {
            state = state.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getMyFavorites()) {
                is Result.Success -> {
                    val items = result.data
                    _favoriteIds.value = items.map { it.id }.toSet()
                    state = state.copy(recipes = items, isLoading = false, errorMessage = null)
                    persistIds(_favoriteIds.value)
                    println("Loaded ${items.size} favorites from server")
                }
                is Result.Error -> {
                    val cached = loadCachedIds()
                    _favoriteIds.value = cached
                    state = state.copy(
                        recipes = emptyList(),
                        isLoading = false,
                        errorMessage = when (result.error) {
                            is AppError.Server -> "Server error ${(result.error as AppError.Server).code}"
                            is AppError.Unauthorized -> "Authentication required"
                            is AppError.Network -> "Network error"
                            else -> "Unknown error"
                        }
                    )
                    println("Failed to load favorites: ${state.errorMessage}")
                }
            }
        }
    }

    /**
     * Toggle favourite. Optimistic: update UI immediately via _favoriteIds and state.recipes.
     * If remote call fails, quietly rollback and persist.
     */
    fun toggleFavorite(recipe: Recipe) {
        scope.launch {
            val currentlyFav = _favoriteIds.value.contains(recipe.id)

            // create snapshots for rollback
            val snapshotIds = _favoriteIds.value
            val snapshotRecipes = state.recipes.toList()

            // optimistic update
            if (currentlyFav) {
                _favoriteIds.value = snapshotIds - recipe.id
            } else {
                _favoriteIds.value = snapshotIds + recipe.id
            }

            // update UI list immediately
            val updatedList = snapshotRecipes.toMutableList()
            if (currentlyFav) updatedList.removeAll { it.id == recipe.id } else updatedList.add(recipe)
            state = state.copy(recipes = updatedList, errorMessage = null)
            persistIds(_favoriteIds.value)
            println("Optimistic update: recipe=${recipe.title}, nowFav=${!currentlyFav}")

            // call backend
            val remoteResult = if (currentlyFav) repository.removeFavorite(recipe.id) else repository.addFavorite(recipe.id)

            when (remoteResult) {
                is Result.Success -> {
                    // server success -> update state with canonical recipe (if provided)
                    val returned = remoteResult.data
                    // replace the recipe in the list with server-returned canonical one (to keep consistent)
                    val newList = state.recipes.toMutableList().apply {
                        removeAll { it.id == returned.id }
                        add(returned)
                    }
                    state = state.copy(recipes = newList, errorMessage = null)
                    _favoriteIds.value = newList.map { it.id }.toSet()
                    persistIds(_favoriteIds.value)
                    println("Server update succeeded for ${recipe.title}")
                }
                is Result.Error -> {
                    // rollback silently
                    println("⚠️ Server update FAILED for ${recipe.title}. Rolling back.")
                    _favoriteIds.value = snapshotIds
                    state = state.copy(recipes = snapshotRecipes, errorMessage = null) // don't surface toggle errors to UI
                    persistIds(_favoriteIds.value)
                }
            }
        }
    }

    fun isFavorite(recipeId: String): Boolean = _favoriteIds.value.contains(recipeId)

    fun clearFavorites() {
        state = state.copy(recipes = emptyList(), errorMessage = null)
        _favoriteIds.value = emptySet()
        persistIds(emptySet())
    }

    private fun persistIds(ids: Set<String>) {
        try {
            settings.putString(FAVORITES_KEY, ids.joinToString(","))
            println("Persisted favorite IDs: $ids")
        } catch (t: Throwable) {
            println("⚠️ Failed to persist favorite IDs: ${t.message}")
        }
    }

    private fun loadCachedIds(): Set<String> {
        return try {
            val raw = settings.getString(FAVORITES_KEY, "")
            if (raw.isBlank()) emptySet() else raw.split(",").filter { it.isNotBlank() }.toSet()
        } catch (t: Throwable) {
            emptySet()
        }
    }
}
