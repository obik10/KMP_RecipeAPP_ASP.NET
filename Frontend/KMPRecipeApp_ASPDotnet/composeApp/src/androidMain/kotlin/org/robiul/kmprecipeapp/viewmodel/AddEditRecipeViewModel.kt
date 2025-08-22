package org.robiul.kmprecipeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddEditUiState(val title: String = "", val instructions: String = "", val saving: Boolean = false)

class AddEditRecipeViewModel(/* inject usecases */) : ViewModel() {
    private val _state = MutableStateFlow(AddEditUiState())
    val state: StateFlow<AddEditUiState> = _state

    fun save() {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true)
            // TODO call Create/Update usecase
            _state.value = _state.value.copy(saving = false)
        }
    }
}
