package org.robiul.kmprecipeapp.core.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import org.robiul.kmprecipeapp.utils.decodeJwtPayload

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
        println("🟢 [TokenStore.save] access=${tokens?.access?.take(30)} refresh=${tokens?.refresh?.take(30)}")
        _tokens.emit(tokens)

        tokens?.access?.let { access ->
            val decoded = decodeJwtPayload(access)
            println("🔐 [JWT Payload] $decoded")
    }
    }

    override suspend fun get(): AuthTokens? {
        val token = tokens.first()
        println("🔵 [TokenStore.get] access=${token?.access?.take(30)}")
        return token
    }

    override suspend fun clear() {
        println("🟡 [TokenStore.clear] Tokens cleared")
        _tokens.emit(null)
    }
}

//class InMemoryAuthTokenStore(initial: AuthTokens? = null) : AuthTokenStore {
//    private val _tokens = MutableStateFlow(initial)
//    override val tokens: StateFlow<AuthTokens?> = _tokens
//
//    override suspend fun save(tokens: AuthTokens?) {
//
//        _tokens.emit(tokens)
//    }
//
//    override suspend fun get(): AuthTokens? {
//        return tokens.first()
//    }
//}

