package org.robiul.kmprecipeapp

import app.cash.sqldelight.db.SqlDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import org.robiul.kmprecipeapp.core.NetworkClient
import org.robiul.kmprecipeapp.core.auth.InMemoryAuthTokenStore2
import org.robiul.kmprecipeapp.data.datasource.LocalDataSource
import org.robiul.kmprecipeapp.data.datasource.RemoteDataSource
import org.robiul.kmprecipeapp.data.repository.RecipeRepositoryImpl
import org.robiul.kmprecipeapp.utils.Result

// --- Dummy SQLDelight driver ---
class DummyDriver2 : SqlDriver {
    override fun close() {}
    override fun addListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) {}
    override fun removeListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) {}
    override fun notifyListeners(vararg queryKeys: String) {}
    override fun currentTransaction(): app.cash.sqldelight.Transacter.Transaction? = null
    override fun newTransaction(): app.cash.sqldelight.db.QueryResult<app.cash.sqldelight.Transacter.Transaction> =
        app.cash.sqldelight.db.QueryResult.Value(object : app.cash.sqldelight.Transacter.Transaction() {
            override val enclosingTransaction: app.cash.sqldelight.Transacter.Transaction? = null
            override fun endTransaction(successful: Boolean) =
                app.cash.sqldelight.db.QueryResult.Value(Unit)
        })

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (app.cash.sqldelight.db.SqlCursor) -> app.cash.sqldelight.db.QueryResult<R>,
        parameters: Int,
        binders: (app.cash.sqldelight.db.SqlPreparedStatement.() -> Unit)?
    ): app.cash.sqldelight.db.QueryResult<R> = throw UnsupportedOperationException("Not used")

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (app.cash.sqldelight.db.SqlPreparedStatement.() -> Unit)?
    ): app.cash.sqldelight.db.QueryResult<Long> = app.cash.sqldelight.db.QueryResult.Value(0L)
}

// --- commonTest: MockEngine ---
class RecipeRepositoryTest {

    private val recipesPaginatedJson = """
        {
          "items": [
            {"id": "1","isExternal": false,"title": "Pasta","instructions": "Boil, toss with sauce","ownerId": "owner-1","imagePath": null,"ingredients": [],"youtubeUrl": null},
            {"id": "2","isExternal": false,"title": "Pizza","instructions": "Bake at 250C","ownerId": "owner-2","imagePath": null,"ingredients": [],"youtubeUrl": null}
          ],
          "totalCount": 2,
          "pageNumber": 1,
          "pageSize": 10,
          "totalPages": 1
        }
    """.trimIndent()

    private val mockEngine = MockEngine { request ->
        respond(
            content = recipesPaginatedJson,
            status = HttpStatusCode.OK,
            headers = headersOf("Content-Type", "application/json")
        )
    }

    private val tokenStore = InMemoryAuthTokenStore2(initial = "dummy_token")
    private val networkClient = NetworkClient(
        baseUrl = "http://localhost:5076", // ignored by MockEngine
        engine = mockEngine,
        tokenStore = tokenStore
    )

    private val remote = RemoteDataSource(networkClient)
    private val local = LocalDataSource(DummyDriver2())
    private val repo = RecipeRepositoryImpl(remote, local)

    @Test
    fun getRecipes_returns_non_empty_list() = runTest {
        val result = repo.getRecipes(pageNumber = 1, pageSize = 10)
        assertTrue(
            result is Result.Success && result.data.isNotEmpty(),
            "Expected non-empty list, got $result"
        )
    }
}
