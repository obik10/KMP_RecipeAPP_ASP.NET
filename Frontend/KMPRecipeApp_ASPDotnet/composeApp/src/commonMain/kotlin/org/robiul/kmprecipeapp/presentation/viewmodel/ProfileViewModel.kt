package org.robiul.kmprecipeapp.presentation.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.robiul.kmprecipeapp.domain.repository.AuthRepository
import org.robiul.kmprecipeapp.utils.Result

class ProfileViewModel(
    private val authRepository: AuthRepository
) {
    var isLoggedIn by mutableStateOf(false)
        private set

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun loadUser() {
        scope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    // ✅ If token exists, we treat user as logged in
                    isLoggedIn = result.data != null
                }
                is Result.Error -> {
                    isLoggedIn = false
                }
            }
        }
    }

    fun logout() {
        scope.launch {
            authRepository.logout()
            isLoggedIn = false
        }
    }
}
