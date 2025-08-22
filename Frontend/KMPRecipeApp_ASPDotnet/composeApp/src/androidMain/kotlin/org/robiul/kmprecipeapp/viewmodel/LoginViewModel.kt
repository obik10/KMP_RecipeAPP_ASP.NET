package org.robiul.kmprecipeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(val email: String = "", val password: String = "", val submitting: Boolean = false, val error: String? = null)

sealed interface LoginEvent {
    object Submit: LoginEvent
    data class Email(val v: String): LoginEvent
    data class Password(val v: String): LoginEvent
}

class LoginViewModel(/* inject usecase here */) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onEvent(e: LoginEvent) {
        when (e) {
            is LoginEvent.Email -> _state.value = _state.value.copy(email = e.v)
            is LoginEvent.Password -> _state.value = _state.value.copy(password = e.v)
            is LoginEvent.Submit -> {
                viewModelScope.launch {
                    _state.value = _state.value.copy(submitting = true, error = null)
                    // TODO: call login usecase and handle result
                    _state.value = _state.value.copy(submitting = false)
                }
            }
        }
    }
}
