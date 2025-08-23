package org.robiul.kmprecipeapp.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.contentOrNull
import org.robiul.kmprecipeapp.Constants
import org.robiul.kmprecipeapp.core.auth.AuthTokenStore
import org.robiul.kmprecipeapp.core.auth.AuthTokens
import org.robiul.kmprecipeapp.core.auth.TokenResponse
import org.robiul.kmprecipeapp.core.currentTimeMillis
import org.robiul.kmprecipeapp.data.datasource.RemoteDataSource
import org.robiul.kmprecipeapp.data.models.dto.RegisterRequest
import org.robiul.kmprecipeapp.domain.models.User
import org.robiul.kmprecipeapp.domain.repository.AuthRepository
import org.robiul.kmprecipeapp.utils.AppError
import org.robiul.kmprecipeapp.utils.Result
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthRepositoryImpl(
    private val remote: RemoteDataSource,
    private val tokenStore: AuthTokenStore,
    private val keycloakUrl: String = Constants.BASE_URL_KEYCLOAK,
    private val client: HttpClient = HttpClient {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
) : AuthRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val httpResponse: HttpResponse = client.submitForm(
                url = "$keycloakUrl${Constants.TOKEN_PATH}",
                formParameters = Parameters.build {
                    append("grant_type", "password")
                    append("username", username)
                    append("password", password)
                    append("client_id", Constants.OAUTH_CLIENT_ID)
                    // request offline_access to encourage issuance of refresh_token
//                    append("scope", "openid offline_access")
//                    Constants.OAUTH_CLIENT_SECRET?.let { append("client_secret", it) }
                }
            )

            val raw = httpResponse.bodyAsText()
            // Temporary debug — replace with proper logging in production
            println("Keycloak token endpoint raw response: $raw")

            if (httpResponse.status.value in 200..299) {
                // Try to decode TokenResponse
                val tokenRes = try {
                    json.decodeFromString<TokenResponse>(raw)
                } catch (ex: Throwable) {
                    return Result.Error(AppError.Server(code = httpResponse.status.value, body = "Failed to parse token response: ${ex.message}; raw: $raw"))
                }

                val tokens = tokenRes.toAuthTokens()
                tokenStore.save(tokens)
                Result.Success(User(id = "self", name = username, email = null))
            } else {
                // Try to parse Keycloak error JSON (it often returns error & error_description)
                val errJson = runCatching { json.parseToJsonElement(raw).jsonObject }.getOrNull()
                val err = errJson?.get("error")?.toString()?.trim('"')
                val desc = errJson?.get("error_description")?.toString()?.trim('"')
                val message = listOfNotNull(err, desc).joinToString(": ").ifEmpty { raw }
                Result.Error(AppError.Server(code = httpResponse.status.value, body = message))
            }
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
            val httpResponse: HttpResponse = client.submitForm(
                url = "$keycloakUrl${Constants.TOKEN_PATH}",
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", current.refresh)
                    append("client_id", Constants.OAUTH_CLIENT_ID)
                    Constants.OAUTH_CLIENT_SECRET?.let { append("client_secret", it) }
                }
            )

            val raw = httpResponse.bodyAsText()
            println("Keycloak refresh endpoint raw response: $raw")

            if (httpResponse.status.value in 200..299) {
                val tokenRes = try {
                    json.decodeFromString<TokenResponse>(raw)
                } catch (ex: Throwable) {
                    tokenStore.clear()
                    return Result.Error(AppError.Server(code = httpResponse.status.value, body = "Failed to parse refresh response: ${ex.message}; raw: $raw"))
                }

                val newTokens = tokenRes.toAuthTokens()
                tokenStore.save(newTokens)
                Result.Success(newTokens.access)
            } else {
                tokenStore.clear()
                val errJson = runCatching { json.parseToJsonElement(raw).jsonObject }.getOrNull()
                val desc = errJson?.get("error_description")?.toString()?.trim('"')
                Result.Error(AppError.Server(code = httpResponse.status.value, body = desc ?: raw))
            }
        } catch (t: Throwable) {
            tokenStore.clear()
            Result.Error(mapThrowable(t))
        }
    }

    override suspend fun register(username: String, email: String, password: String): Result<Unit> =
        remote.register(RegisterRequest(username, email, password))

    override suspend fun saveToken(token: String?): Result<Unit> {
        if (token == null) tokenStore.clear() else {
            val existing = tokenStore.get()
            if (existing != null) tokenStore.save(existing.copy(access = token))
        }
        return Result.Success(Unit)
    }

    override suspend fun currentToken(): Result<String?> =
        Result.Success(tokenStore.get()?.access)

    private fun TokenResponse.toAuthTokens(): AuthTokens {
        val now = currentTimeMillis()
        val exp = now + (expiresIn * 1000L) - Constants.EXPIRY_SKEW_MS
        return AuthTokens(
            access = accessToken,
            // if refreshToken is null, store empty string (you may choose different behavior)
            refresh = refreshToken ?: "",
            expiresAtMillis = exp
        )
    }

    private suspend fun mapThrowable(t: Throwable): AppError = when (t) {
        is AppError -> t
        is io.ktor.client.plugins.ClientRequestException,
        is io.ktor.client.plugins.ServerResponseException -> {
            val responseText = (t as? io.ktor.client.plugins.ClientRequestException)?.response?.bodyAsText()
                ?: (t as? io.ktor.client.plugins.ServerResponseException)?.response?.bodyAsText()

            val errJson = runCatching {
                responseText?.let { json.parseToJsonElement(it).jsonObject }
            }.getOrNull()
            val desc = errJson?.get("error_description")?.jsonPrimitive?.contentOrNull
            val err = errJson?.get("error")?.jsonPrimitive?.contentOrNull
            AppError.Server(
                code = (t as? io.ktor.client.plugins.ResponseException)?.response?.status?.value ?: -1,
                body = desc ?: err ?: responseText ?: "Request failed"
            )
        }
        else -> AppError.Unknown(t.message, t)
    }

}
