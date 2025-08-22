package org.robiul.kmprecipeapp.data.datasource

import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import org.robiul.kmprecipeapp.core.NetworkClient
import org.robiul.kmprecipeapp.data.models.dto.*
import org.robiul.kmprecipeapp.utils.Result

class RemoteDataSource(
    private val client: NetworkClient
) {
    // Auth
    suspend fun register(request: RegisterRequest): Result<Unit> =
        client.post<RegisterRequest, Unit>("/api/Auth/register", request, authRequired = false)

    // Recipes
    suspend fun listPaginated(pageNumber: Int, pageSize: Int): Result<RecipeDtoPaginatedResult> =
        client.get("/api/Recipes", query = mapOf("pageNumber" to pageNumber, "pageSize" to pageSize))

    suspend fun search(keyword: String): Result<List<RecipeDto>> =
        client.get("/api/Recipes/search", query = mapOf("keyword" to keyword))

    suspend fun getById(id: String): Result<RecipeDto> =
        client.get("/api/Recipes/$id")

    suspend fun create(body: CreateRecipeRequest): Result<RecipeDto> =
        client.post("/api/Recipes", body, authRequired = true)

    suspend fun update(id: String, body: UpdateRecipeRequest): Result<RecipeDto> =
        client.put("/api/Recipes/$id", body, authRequired = true)

    suspend fun delete(id: String): Result<Unit> =
        client.delete("/api/Recipes/$id", authRequired = true)

    suspend fun uploadImage(id: String, fileName: String, bytes: ByteArray): Result<RecipeDto> {
        val form = formData {
            append(
                key = "file",
                value = bytes,
                headers = Headers.build {
                    append(
                        HttpHeaders.ContentDisposition,
                        "form-data; name=\"file\"; filename=\"$fileName\""
                    )
                    append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                }
            )
        }

        return client.postMultipart(
            path = "/api/Recipes/$id/image-recipe",
            formData = FormDataContent(form as Parameters),
            authRequired = true
        )
    }


    suspend fun myRecipes(): Result<List<RecipeDto>> =
        client.get("/api/Recipes/myrecipes", authRequired = true)

    suspend fun addFavorite(id: String): Result<RecipeDto> =
        client.post<Unit, RecipeDto>("/api/Recipes/$id/add-favorite", Unit, authRequired = true)

    suspend fun removeFavorite(id: String): Result<RecipeDto> =
        client.post<Unit, RecipeDto>("/api/Recipes/$id/delete-favorite", Unit, authRequired = true)

    suspend fun myFavorites(): Result<List<RecipeDto>> =
        client.get("/api/Recipes/myfavorites", authRequired = true)
}
