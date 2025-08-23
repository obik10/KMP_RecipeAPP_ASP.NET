package org.robiul.kmprecipeapp.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.robiul.kmprecipeapp.Constants
import org.robiul.kmprecipeapp.core.auth.AuthTokenStore
import org.robiul.kmprecipeapp.core.auth.AuthTokens
import org.robiul.kmprecipeapp.core.auth.TokenResponse
import org.robiul.kmprecipeapp.core.currentTimeMillis
import org.robiul.kmprecipeapp.data.datasource.RemoteDataSource
import org.robiul.kmprecipeapp.domain.models.User
import org.robiul.kmprecipeapp.domain.repository.AuthRepository
import org.robiul.kmprecipeapp.utils.AppError
import org.robiul.kmprecipeapp.utils.Result

class AuthRepositoryImpl(
    private val remote: RemoteDataSource,
    private val tokenStore: AuthTokenStore,
    private val baseUrl: String = Constants.BASE_URL_DEBUG,
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val tokenRes: TokenResponse = client.submitForm(
                url = "$baseUrl${Constants.TOKEN_PATH}",
                formParameters = Parameters.build {
                    append("grant_type", "password")
                    append("username", username)
                    append("password", password)
                    append("client_id", Constants.OAUTH_CLIENT_ID)
                    Constants.OAUTH_CLIENT_SECRET?.let { append("client_secret", it) }
                }
            ).body()

            val tokens = tokenRes.toAuthTokens()
            tokenStore.save(tokens)

            Result.Success(User(id = "self", name = username, email = null))
        } catch (t: Throwable) {
            Result.Error(mapThrowable(t))
        }
    }

    override suspend fun logout(): Result<Unit> {
        tokenStore.clear()
        return Result.Success(Unit)
    }

    override suspend fun refreshToken(): Result<String> {
        val current = tokenStore.get() ?: return Result.Error(AppError.Unauthorized)
        return try {
            val tokenRes: TokenResponse = client.submitForm(
                url = "$baseUrl${Constants.TOKEN_PATH}",
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", current.refresh)
                    append("client_id", Constants.OAUTH_CLIENT_ID)
                    Constants.OAUTH_CLIENT_SECRET?.let { append("client_secret", it) }
                }
            ).body()

            val newTokens = tokenRes.toAuthTokens()
            tokenStore.save(newTokens)
            Result.Success(newTokens.access)
        } catch (t: Throwable) {
            tokenStore.clear()
            Result.Error(mapThrowable(t))
        }
    }

    override suspend fun register(username: String, email: String, password: String): Result<Unit> =
        remote.register(org.robiul.kmprecipeapp.data.models.dto.RegisterRequest(username, email, password))

    override suspend fun saveToken(token: String?): Result<Unit> {
        if (token == null) tokenStore.clear() else {
            val existing = tokenStore.get()
            if (existing != null) tokenStore.save(existing.copy(access = token))
        }
        return Result.Success(Unit)
    }

    override suspend fun currentToken(): Result<String?> = Result.Success(tokenStore.get()?.access)

    private fun TokenResponse.toAuthTokens(): AuthTokens {
        val now = currentTimeMillis()
        val exp = now + (expiresIn * 1000L) - Constants.EXPIRY_SKEW_MS
        return AuthTokens(
            access = accessToken ?: "",
            refresh = refreshToken ?: "",
            expiresAtMillis = exp
        )
    }

    private fun mapThrowable(t: Throwable): AppError = when (t) {
        is AppError -> t
        else -> AppError.Unknown(t.message, t)
    }
}
