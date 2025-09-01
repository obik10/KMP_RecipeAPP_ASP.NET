package org.robiul.kmprecipeapp.presentation.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.robiul.kmprecipeapp.domain.usecase.RegisterUser
import org.robiul.kmprecipeapp.utils.Result

class RegisterViewModel(
    private val registerUser: RegisterUser
) {
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    private val scope = CoroutineScope(Dispatchers.Main)

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        scope.launch {
            isLoading = true
            errorMessage = null

            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                errorMessage = "Fill all fields"
                isLoading = false
                return@launch
            }

            when (val result = registerUser(name, email, password)) {
                is Result.Success -> {
                    onSuccess()
                }
                is Result.Error -> {
                    errorMessage = result.error.message ?: "Registration failed"
                }
            }

            isLoading = false
        }
    }
}
