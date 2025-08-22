package org.robiul.kmprecipeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RecipeDetailUiState(val id: String = "", val title: String = "", val instructions: String = "", val loading: Boolean = true)

class RecipeDetailViewModel(/* inject usecases */) : ViewModel() {
    private val _state = MutableStateFlow(RecipeDetailUiState())
    val state: StateFlow<RecipeDetailUiState> = _state

    fun load(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            // TODO: load from usecase
            _state.value = _state.value.copy(id = id, title = "Recipe $id", instructions = "...", loading = false)
        }
    }
}
