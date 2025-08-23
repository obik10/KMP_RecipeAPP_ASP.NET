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
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.robiul.kmprecipeapp.core.auth.AuthTokenStore
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

    // --- PUBLIC API ---

    suspend inline fun <reified T : Any> get(
        path: String,
        authRequired: Boolean = false,
        query: Map<String, Any?> = emptyMap()
    ): Result<T> = safe {
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
    ): Result<Res> = safe {
        val builder = HttpRequestBuilder()
        builder.url.takeFrom(URLBuilder(baseUrl).apply { encodedPath = "" }.build())
        builder.url.appendPathSegments(path.trimStart('/'))

        if (authRequired) tokenStore.get()?.access?.let { token ->
            builder.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }

        builder.setBody(body)
        client.post(builder)
    }

    suspend inline fun <reified Res : Any> postMultipart(
        path: String,
        formData: io.ktor.client.request.forms.FormDataContent,
        authRequired: Boolean = true
    ): Result<Res> = safe {
        val builder = HttpRequestBuilder()
        builder.url.takeFrom(URLBuilder(baseUrl).apply { encodedPath = "" }.build())
        builder.url.appendPathSegments(path.trimStart('/'))

        if (authRequired) tokenStore.get()?.access?.let { token ->
            builder.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }

        builder.setBody(formData)
        client.post(builder)
    }

    suspend inline fun <reified Req : Any, reified Res : Any> put(
        path: String,
        body: Req,
        authRequired: Boolean = false
    ): Result<Res> = safe {
        val builder = HttpRequestBuilder()
        builder.url.takeFrom(URLBuilder(baseUrl).apply { encodedPath = "" }.build())
        builder.url.appendPathSegments(path.trimStart('/'))

        if (authRequired) tokenStore.get()?.access?.let { token ->
            builder.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }

        builder.setBody(body)
        client.put(builder)
    }

    suspend inline fun <reified Res : Any> delete(
        path: String,
        authRequired: Boolean = false
    ): Result<Res> = safe {
        val builder = HttpRequestBuilder()
        builder.url.takeFrom(URLBuilder(baseUrl).apply { encodedPath = "" }.build())
        builder.url.appendPathSegments(path.trimStart('/'))

        if (authRequired) tokenStore.get()?.access?.let { token ->
            builder.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }

        client.delete(builder)
    }

    // --- SAFE WRAPPER (basic for T4; single-flight refresh comes in T5) ---

    suspend inline fun <reified T : Any> safe(block: suspend () -> HttpResponse): Result<T> {
        return try {
            var response = block()

            // Minimal: if 401 and we *already* have a non-null token, surface Unauthorized.
            // In T5 we will call AuthRepository.refresh() with a Mutex and retry once.
            if (response.status.value == 401) {
                return Result.Error(AppError.Unauthorized)
            }

            if (response.status.value in 200..299) {
                Result.Success(response.body())
            } else {
                val errorText = runCatching { response.body<ErrorResponse>() }.getOrNull()
                Result.Error(AppError.Server(
                    code = response.status.value,
                    body = errorText?.message ?: response.toString()
                ))
            }
        } catch (e: AppError) {
            Result.Error(e)
        } catch (t: Throwable) {
            Result.Error(AppError.Unknown(t.message, t))
        }
    }
}

@Serializable
data class ErrorResponse(
    @SerialName("message") val message: String? = null,
    @SerialName("code") val code: Int? = null
)
