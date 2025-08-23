package org.robiul.kmprecipeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.robiul.kmprecipeapp.core.auth.AuthTokenStore
import org.robiul.kmprecipeapp.domain.repository.AuthRepository
import org.robiul.kmprecipeapp.utils.AppError
import org.robiul.kmprecipeapp.utils.Result

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel(), KoinComponent {
    private val authRepository: AuthRepository by inject()
    private val tokenStore: AuthTokenStore by inject()

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    // Expose tokens to NavGraph for auto-routing
    val tokens = tokenStore.tokens

    fun onEmailChanged(v: String) { _state.update { it.copy(email = v) } }
    fun onPasswordChanged(v: String) { _state.update { it.copy(password = v) } }

    fun submitLogin(onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        val s = state.value
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            when (val r = authRepository.login(s.email.trim(), s.password)) {
                is Result.Success -> {
                    _state.update { it.copy(submitting = false) }
                    onResult(true, null)
                }
                is Result.Error -> {
                    val msg = r.error.toHumanMessage(defaultMsg = "Login failed")
                    _state.update { it.copy(submitting = false, error = msg) }
                    onResult(false, msg)
                }
            }
        }
    }

    fun register(username: String, email: String, password: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            when (val r = authRepository.register(username.trim(), email.trim(), password)) {
                is Result.Success -> {
                    _state.update { it.copy(submitting = false) }
                    onResult(true, null)
                }
                is Result.Error -> {
                    val msg = r.error.toHumanMessage(defaultMsg = "Registration failed")
                    _state.update { it.copy(submitting = false, error = msg) }
                    onResult(false, msg)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // tokenStore cleared by repository
        }
    }

    private fun AppError.toHumanMessage(defaultMsg: String): String = when (this) {
        is AppError.Server -> {
            if (code in 400..499) "Invalid credentials or request"
            else "Server error (${code ?: "unknown"})"
        }
        is AppError.Unauthorized -> "Unauthorized"
        is AppError.Network -> "Network error"
        else -> message ?: defaultMsg
    }
}
