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
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.forms.submitForm
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.http.Parameters
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
    val baseUrl: String,
    engine: HttpClientEngine,
    val tokenStore: AuthTokenStore
) {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true; coerceInputValues = true }

    val client: HttpClient = HttpClient(engine) {
        install(ContentNegotiation) { json(json) }
        install(Logging) {
            logger = object : Logger { override fun log(message: String) { /* route to platform logger, but never log tokens */ } }
            level = LogLevel.INFO
        }
        install(DefaultRequest) {
            url(baseUrl)
            accept(ContentType.Application.Json)
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }
        install(HttpRequestRetry) { retryOnServerErrors(maxRetries = 2); exponentialDelay() }
    }

    // --- SAFE WRAPPER: attempts one refresh on 401 for authRequired requests ---
    suspend inline fun <reified T : Any> safe(
        authRequired: Boolean = false,
        crossinline block: suspend () -> HttpResponse
    ): Result<T> {
        try {
            var response = block()

            if (response.status.value == 401 && authRequired) {
                // Try one-time refresh using Keycloak (avoid circular DI by calling the token endpoint directly)
                val refreshed = attemptRefresh()
                if (!refreshed) {
                    tokenStore.clear()
                    return Result.Error(AppError.Unauthorized)
                }
                // Retry original request once (builder will re-read token from tokenStore)
                response = block()
            }

            if (response.status.value in 200..299) {
                return Result.Success(response.body())
            } else {
                val errorText = runCatching { response.body<ErrorResponse>() }.getOrNull()
                return Result.Error(
                    AppError.Server(
                        code = response.status.value,
                        body = errorText?.message ?: response.toString()
                    )
                )
            }
        } catch (e: AppError) {
            return Result.Error(e)
        } catch (t: Throwable) {
            return Result.Error(AppError.Unknown(t.message, t))
        }
    }

    // Attempt to refresh token via Keycloak token endpoint and save into tokenStore.
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
            val newTokens = AuthTokens(
                access = tokenRes.accessToken ?: "",
                refresh = tokenRes.refreshToken ?: "",
                expiresAtMillis = exp
            )
            tokenStore.save(newTokens)
            true
        } catch (t: Throwable) {
            tokenStore.clear()
            false
        }
    }

    // ------------------ helpers that build requests ------------------

    suspend inline fun <reified T : Any> get(
        path: String,
        authRequired: Boolean = false,
        query: Map<String, Any?> = emptyMap()
    ): Result<T> = safe(authRequired) {
        val builder = HttpRequestBuilder()
        builder.url.takeFrom(URLBuilder(baseUrl).apply { encodedPath = "" }.build())
        builder.url.appendPathSegments(path.trimStart('/'))
        query.forEach { (k, v) -> if (v != null) builder.parameter(k, v) }

        if (authRequired) tokenStore.get()?.access?.let { token ->
            builder.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }

        client.get(builder)
    }

    suspend inline fun <reified Req : Any, reified Res : Any> post(
        path: String,
        body: Req,
        authRequired: Boolean = false
    ): Result<Res> = safe(authRequired) {
        val builder = HttpRequestBuilder()
        builder.url.takeFrom(URLBuilder(baseUrl).apply { encodedPath = "" }.build())
        builder.url.appendPathSegments(path.trimStart('/'))

        if (authRequired) tokenStore.get()?.access?.let { token ->
            builder.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }

        builder.setBody(body)
        client.post(builder)
    }

    suspend inline fun <reified Req : Any, reified Res : Any> put(
        path: String,
        body: Req,
        authRequired: Boolean = false
    ): Result<Res> = safe(authRequired) {
        val builder = HttpRequestBuilder()
        builder.url.takeFrom(URLBuilder(baseUrl).apply { encodedPath = "" }.build())
        builder.url.appendPathSegments(path.trimStart('/'))

        if (authRequired) tokenStore.get()?.access?.let { token ->
            builder.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }

        builder.setBody(body)
        client.put(builder)
    }

    suspend inline fun <reified Res : Any> postMultipart(
        path: String,
        formData: MultiPartFormDataContent,
        authRequired: Boolean = true
    ): Result<Res> = safe(authRequired) {
        val builder = HttpRequestBuilder()
        builder.url.takeFrom(URLBuilder(baseUrl).apply { encodedPath = "" }.build())
        builder.url.appendPathSegments(path.trimStart('/'))

        if (authRequired) tokenStore.get()?.access?.let { token ->
            builder.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }

        builder.setBody(formData)
        client.post(builder)
    }

    suspend inline fun <reified Res : Any> delete(
        path: String,
        authRequired: Boolean = false
    ): Result<Res> = safe(authRequired) {
        val builder = HttpRequestBuilder()
        builder.url.takeFrom(URLBuilder(baseUrl).apply { encodedPath = "" }.build())
        builder.url.appendPathSegments(path.trimStart('/'))

        if (authRequired) tokenStore.get()?.access?.let { token ->
            builder.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }

        client.delete(builder)
    }
}

@Serializable
data class ErrorResponse(
    @SerialName("message") val message: String? = null,
    @SerialName("code") val code: Int? = null
)
