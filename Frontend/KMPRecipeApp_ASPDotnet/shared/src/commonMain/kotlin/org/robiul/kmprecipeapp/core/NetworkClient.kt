package org.robiul.kmprecipeapp.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.submitForm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.robiul.kmprecipeapp.Constants
import org.robiul.kmprecipeapp.core.auth.AuthTokenStore
import org.robiul.kmprecipeapp.core.auth.AuthTokens
import org.robiul.kmprecipeapp.core.auth.TokenResponse
import org.robiul.kmprecipeapp.core.currentTimeMillis
import org.robiul.kmprecipeapp.utils.AppError
import org.robiul.kmprecipeapp.utils.Result

class NetworkClient(
    private val baseUrl: String,
    engine: HttpClientEngine,
    val tokenStore: AuthTokenStore
) {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true; coerceInputValues = true }

    val client = HttpClient(engine) {
        install(ContentNegotiation) { json(json) }
        install(Logging) {
            logger = object : Logger { override fun log(message: String) { /* no token logging */ } }
            level = LogLevel.INFO
        }
        install(DefaultRequest) {
            url(baseUrl)
            accept(ContentType.Application.Json)
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 2)
            exponentialDelay()
        }
    }

    // ---------------- Safe wrapper ----------------
    suspend inline fun <reified T : Any> safe(
        authRequired: Boolean = false,
        crossinline block: suspend (accessToken: String?) -> HttpResponse
    ): Result<T> {
        try {
            val token = tokenStore.get()?.access
            var response = block(token)

            if (response.status == HttpStatusCode.Unauthorized && authRequired) {
                val refreshed = attemptRefresh()
                if (!refreshed) {
                    tokenStore.clear()
                    return Result.Error(AppError.Unauthorized)
                }
                val newToken = tokenStore.get()?.access
                response = block(newToken)
            }

            return if (response.status.value in 200..299) {
                Result.Success(response.body())
            } else {
                val errorText = runCatching { response.body<ErrorResponse>() }.getOrNull()
                Result.Error(AppError.Server(response.status.value, errorText?.message ?: response.toString()))
            }

        } catch (e: AppError) {
            return Result.Error(e)
        } catch (t: Throwable) {
            return Result.Error(AppError.Unknown(t.message, t))
        }
    }

    // ---------------- Token Refresh ----------------
    suspend fun attemptRefresh(): Boolean {
        val current = tokenStore.get() ?: return false
        return try {
            val tokenRes: TokenResponse = client.submitForm(
                url = "${Constants.BASE_URL_KEYCLOAK}${Constants.TOKEN_PATH}",
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", current.refresh)
                    append("client_id", Constants.OAUTH_CLIENT_ID)
                    Constants.OAUTH_CLIENT_SECRET?.let { append("client_secret", it) }
                }
            ).body()

            val now = currentTimeMillis()
            val exp = now + (tokenRes.expiresIn * 1000L) - Constants.EXPIRY_SKEW_MS
            tokenStore.save(
                AuthTokens(
                    access = tokenRes.accessToken ?: "",
                    refresh = tokenRes.refreshToken ?: "",
                    expiresAtMillis = exp
                )
            )
            true
        } catch (t: Throwable) {
            tokenStore.clear()
            false
        }
    }

    // ---------------- HTTP Methods ----------------
    suspend inline fun <reified T : Any> get(
        path: String,
        authRequired: Boolean = false,
        query: Map<String, Any?> = emptyMap()
    ): Result<T> = safe(authRequired) { token ->
        client.get(buildRequest(path, authRequired, token, query))
    }

    suspend inline fun <reified Req : Any, reified Res : Any> post(
        path: String,
        body: Req,
        authRequired: Boolean = false
    ): Result<Res> = safe(authRequired) { token ->
        client.post(buildRequest(path, authRequired, token).apply { setBody(body) })
    }

    suspend inline fun <reified Req : Any, reified Res : Any> put(
        path: String,
        body: Req,
        authRequired: Boolean = false
    ): Result<Res> = safe(authRequired) { token ->
        client.put(buildRequest(path, authRequired, token).apply { setBody(body) })
    }

    suspend inline fun <reified Res : Any> delete(
        path: String,
        authRequired: Boolean = false
    ): Result<Res> = safe(authRequired) { token ->
        client.delete(buildRequest(path, authRequired, token))
    }

    suspend inline fun <reified Res : Any> postMultipart(
        path: String,
        formData: MultiPartFormDataContent,
        authRequired: Boolean = true
    ): Result<Res> = safe(authRequired) { token ->
        client.post(buildRequest(path, authRequired, token).apply { setBody(formData) })
    }

    // ---------------- Build Request Helper ----------------
    fun buildRequest(
        path: String,
        authRequired: Boolean,
        token: String?,
        query: Map<String, Any?> = emptyMap()
    ): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url.takeFrom(URLBuilder(baseUrl).apply { encodedPath = "" })
            url.appendPathSegments(path.trimStart('/'))
            query.forEach { (k, v) -> if (v != null) parameter(k, v) }
            if (authRequired && !token.isNullOrBlank()) {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }
}

@Serializable
data class ErrorResponse(
    @SerialName("message") val message: String? = null,
    @SerialName("code") val code: Int? = null
)
