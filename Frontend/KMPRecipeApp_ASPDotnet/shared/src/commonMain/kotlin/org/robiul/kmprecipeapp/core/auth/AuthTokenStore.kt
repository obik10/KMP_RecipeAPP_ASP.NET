package org.robiul.kmprecipeapp.core.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
interface AuthTokenStore {
    val tokens: StateFlow<AuthTokens?>
    suspend fun save(tokens: AuthTokens?)
    suspend fun get(): AuthTokens?  // convenience
    suspend fun clear() = save(null)
}

class InMemoryAuthTokenStore(initial: AuthTokens? = null) : AuthTokenStore {
    private val _tokens = MutableStateFlow(initial)
    override val tokens: StateFlow<AuthTokens?> = _tokens

    override suspend fun save(tokens: AuthTokens?) {
        _tokens.emit(tokens)
    }

    override suspend fun get(): AuthTokens? {
        return tokens.first()
    }
}

