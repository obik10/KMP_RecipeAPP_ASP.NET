package org.robiul.kmprecipeapp.core.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

interface AuthTokenStore {
    val token: StateFlow<String?>
    suspend fun save(token: String?)
    suspend fun get(): String? = token.first()
    suspend fun clear() = save(null)

    suspend fun refreshToken(): String?

}

class InMemoryAuthTokenStore(initial: String? = null) : AuthTokenStore {
    private val _token = MutableStateFlow(initial)
    override val token: StateFlow<String?> = _token
    override suspend fun save(token: String?) { _token.emit(token) }

    override suspend fun refreshToken(): String? {
        // minimal stub: just return the current token
        // (real impl will call Keycloak refresh later)
        return token.value
    }
}
