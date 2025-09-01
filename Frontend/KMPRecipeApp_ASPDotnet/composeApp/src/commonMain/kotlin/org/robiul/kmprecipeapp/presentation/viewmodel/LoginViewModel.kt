package org.robiul.kmprecipeapp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.robiul.kmprecipeapp.domain.repository.AuthRepository
import org.robiul.kmprecipeapp.utils.AppError
import org.robiul.kmprecipeapp.utils.Result

class LoginViewModel(
    private val authRepository: AuthRepository
) {
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (isLoading) return
        isLoading = true
        errorMessage = null

        scope.launch {
            when (val r = authRepository.login(email.trim(), password)) {
                is Result.Success -> {
                    // ✅ Load user info after login
                    val userResult = authRepository.getCurrentUser()
                    if (userResult is Result.Success && userResult.data != null) {
                        println("Logged in as ${userResult.data!!.name} (${userResult.data!!.email})")
                    }

                    isLoading = false
                    errorMessage = null
                    onSuccess()
                }
                is Result.Error -> {
                    isLoading = false
                    errorMessage = r.error.toHumanMessage("Login failed")
                }
            }
        }
    }

    private fun AppError.toHumanMessage(defaultMsg: String): String = when (this) {
        is AppError.Server ->
            if (code in 400..499) "Invalid credentials"
            else "Server error (${code ?: "unknown"})"
        is AppError.Unauthorized -> "Unauthorized"
        is AppError.Network -> "Network error"
        else -> message ?: defaultMsg
    }
}
