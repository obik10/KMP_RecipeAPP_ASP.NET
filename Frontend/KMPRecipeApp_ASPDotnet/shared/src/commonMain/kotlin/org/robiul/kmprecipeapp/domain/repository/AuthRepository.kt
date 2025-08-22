package org.robiul.kmprecipeapp.domain.repository

import org.robiul.kmprecipeapp.domain.models.User
import org.robiul.kmprecipeapp.utils.Result

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): Result<String>

    // Phase 7 additions (register & token helpers)
    suspend fun register(username: String, email: String, password: String): Result<Unit>
    suspend fun saveToken(token: String?): Result<Unit>
    suspend fun currentToken(): Result<String?>
}
