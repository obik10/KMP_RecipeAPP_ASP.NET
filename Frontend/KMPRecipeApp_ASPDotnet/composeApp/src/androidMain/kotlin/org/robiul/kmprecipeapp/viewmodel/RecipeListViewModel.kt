package org.robiul.kmprecipeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RecipeListUiState(val items: List<String> = emptyList(), val isLoading: Boolean = false, val page: Int = 1, val reachingEnd: Boolean = false)

class RecipeListViewModel(/* inject usecases */) : ViewModel() {
    private val _state = MutableStateFlow(RecipeListUiState())
    val state: StateFlow<RecipeListUiState> = _state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            // load cache first (TODO)
            // then trigger network refresh (TODO)
            _state.value = _state.value.copy(items = List(10) { "recipe-${it + 1}" }, isLoading = false)
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            if (_state.value.reachingEnd) return@launch
            val next = _state.value.page + 1
            // TODO: append results
            _state.value = _state.value.copy(page = next)
        }
    }
}
