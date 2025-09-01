package org.robiul.kmprecipeapp.data.datasource

import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import org.robiul.kmprecipeapp.core.NetworkClient
import org.robiul.kmprecipeapp.data.models.dto.*
import org.robiul.kmprecipeapp.utils.Result

class RemoteDataSource(
    private val client: NetworkClient
) {

    // --- Auth ---
    suspend fun register(request: RegisterRequest): Result<Unit> =
        client.post<RegisterRequest, Unit>(
            path = "/api/Auth/register",
            body = request,
            authRequired = false
        )

    // --- Recipes ---
    suspend fun listPaginated(pageNumber: Int, pageSize: Int): Result<RecipeDtoPaginatedResult> =
        client.get(
            path = "/api/Recipes",
            query = mapOf("pageNumber" to pageNumber, "pageSize" to pageSize)
        )

    suspend fun search(keyword: String): Result<List<RecipeDto>> =
        client.get(
            path = "/api/Recipes/search",
            query = mapOf("keyword" to keyword)
        )

    suspend fun getById(id: String): Result<RecipeDto> =
        client.get("/api/Recipes/$id")

    suspend fun create(body: CreateRecipeRequest): Result<RecipeDto> =
        client.post("/api/Recipes", body, authRequired = true)

    suspend fun update(id: String, body: UpdateRecipeRequest): Result<RecipeDto> =
        client.put("/api/Recipes/$id", body, authRequired = true)

    suspend fun delete(id: String): Result<Unit> =
        client.delete("/api/Recipes/$id", authRequired = true)

    suspend fun uploadImage(id: String, fileName: String, bytes: ByteArray): Result<RecipeDto> {
        // Optional: a simple mime detection
        val mime = when {
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> ContentType.Image.JPEG
            fileName.endsWith(".png", true) -> ContentType.Image.PNG
            else -> ContentType.Application.OctetStream
        }

        val multipart = MultiPartFormDataContent(formData {
            // IMPORTANT: backend expects "File", not "file"
            append(
                key = "File",
                value = bytes,
                headers = Headers.build {
                    append(
                        HttpHeaders.ContentDisposition,
                        "form-data; name=\"File\"; filename=\"$fileName\""
                    )
                    append(HttpHeaders.ContentType, mime.toString())
                }
            )
        })

        return client.postMultipart(
            path = "/api/Recipes/$id/image-recipe",
            formData = multipart,
            authRequired = true
        )
    }


suspend fun myRecipes(): Result<List<RecipeDto>> =
        client.get("/api/Recipes/myrecipes", authRequired = true)

    suspend fun addFavorite(id: String): Result<RecipeDto> =
        client.post<Unit, RecipeDto>("/api/Recipes/$id/add-favorite", Unit, authRequired = true)

    suspend fun removeFavorite(id: String): Result<RecipeDto> {
        println("🔹 Sending DELETE to /api/Recipes/$id/delete-favorite")
        val result = client.delete<RecipeDto>("/api/Recipes/$id/delete-favorite", authRequired = true)
        println("🔹 Response: $result")
        return result
    }

    suspend fun myFavorites(): Result<List<RecipeDto>> =
        client.get("/api/Recipes/myfavorites", authRequired = true)
}
