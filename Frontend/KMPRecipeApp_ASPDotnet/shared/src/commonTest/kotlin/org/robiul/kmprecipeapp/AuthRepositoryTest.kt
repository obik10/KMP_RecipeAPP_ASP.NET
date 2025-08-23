package org.robiul.kmprecipeapp

import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.robiul.kmprecipeapp.core.NetworkClient
import org.robiul.kmprecipeapp.core.auth.InMemoryAuthTokenStore2
import org.robiul.kmprecipeapp.data.datasource.RemoteDataSource
import org.robiul.kmprecipeapp.data.repository.AuthRepositoryImpl
import org.robiul.kmprecipeapp.utils.Result

// --- Fake SQLDelight driver for tests ---
class DummyDriver : SqlDriver {
    override fun close() {}
    override fun addListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) {}
    override fun removeListener(vararg queryKeys: String, listener: app.cash.sqldelight.Query.Listener) {}
    override fun notifyListeners(vararg queryKeys: String) {}
    override fun currentTransaction(): app.cash.sqldelight.Transacter.Transaction? = null
    override fun newTransaction(): app.cash.sqldelight.db.QueryResult<app.cash.sqldelight.Transacter.Transaction> =
        app.cash.sqldelight.db.QueryResult.Value(object : app.cash.sqldelight.Transacter.Transaction() {
            override val enclosingTransaction: app.cash.sqldelight.Transacter.Transaction? = null
            override fun endTransaction(successful: Boolean): app.cash.sqldelight.db.QueryResult<Unit> =
                app.cash.sqldelight.db.QueryResult.Value(Unit)
        })

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (app.cash.sqldelight.db.SqlCursor) -> app.cash.sqldelight.db.QueryResult<R>,
        parameters: Int,
        binders: (app.cash.sqldelight.db.SqlPreparedStatement.() -> Unit)?
    ): app.cash.sqldelight.db.QueryResult<R> {
        throw UnsupportedOperationException("Not used in this test")
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (app.cash.sqldelight.db.SqlPreparedStatement.() -> Unit)?
    ): app.cash.sqldelight.db.QueryResult<Long> =
        app.cash.sqldelight.db.QueryResult.Value(0L)
}

// --- The test ---
class AuthRepositoryTest {

    private val loginResponseJson = """
        {
          "token": "dummy_token",
          "expiresIn": 3600,
          "user": {
            "id": "1",
            "email": "any@example.com"
          }
        }
    """.trimIndent()

    // MockEngine works in commonTest on all platforms
    private val mockEngine = MockEngine { request ->
        when {
            request.url.encodedPath.contains("/api/Auth/login", ignoreCase = true) ->
                respond(
                    content = loginResponseJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json")
                )

            else ->
                respond(
                    content = "{}",
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type", "application/json")
                )
        }
    }

    private val tokenStore = InMemoryAuthTokenStore2(initial = null)
    private val networkClient = NetworkClient(
        baseUrl = "http://localhost", // ignored by MockEngine
        engine = mockEngine,
        tokenStore = tokenStore
    )

    private val remote = RemoteDataSource(networkClient)

    @Test
    fun login_returns_dummy_success() = runTest {
        val repo = AuthRepositoryImpl(remote, tokenStore) // ✅ pass both
        val result = repo.login("any", "pass")
        assertTrue(result is Result.Success)
    }

}
