package org.robiul.kmprecipeapp.data.repository

import org.robiul.kmprecipeapp.core.auth.AuthTokenStore
import org.robiul.kmprecipeapp.data.datasource.RemoteDataSource
import org.robiul.kmprecipeapp.data.models.dto.RegisterRequest
import org.robiul.kmprecipeapp.domain.models.User
import org.robiul.kmprecipeapp.domain.repository.AuthRepository
import org.robiul.kmprecipeapp.utils.AppError
import org.robiul.kmprecipeapp.utils.Result

class AuthRepositoryImpl(
    private val remote: RemoteDataSource,
    private val tokenStore: AuthTokenStore
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        // Placeholder login logic for Phase 7 (Keycloak planned later)
        return Result.Success(User(id = "temp", name = username, email = "$username@example.com"))
    }

    override suspend fun logout(): Result<Unit> {
        tokenStore.clear()
        return Result.Success(Unit)
    }

    override suspend fun refreshToken(): Result<String> {
        val t = tokenStore.get()
        return if (t != null) Result.Success(t) else Result.Error(AppError.Unauthorized)
    }

    override suspend fun register(username: String, email: String, password: String): Result<Unit> {
        // We expect remote.register to return Result<Unit>
        return remote.register(RegisterRequest(username, email, password))
    }

    override suspend fun saveToken(token: String?): Result<Unit> {
        tokenStore.save(token)
        return Result.Success(Unit)
    }

    override suspend fun currentToken(): Result<String?> = Result.Success(tokenStore.get())
}
