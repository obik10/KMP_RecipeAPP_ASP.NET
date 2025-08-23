package org.robiul.kmprecipeapp.core.auth

import kotlinx.coroutines.flow.StateFlow

class InMemoryAuthTokenStore2(initial: AuthTokens? = null): AuthTokenStore {
    override val tokens: StateFlow<AuthTokens?>
        get() = TODO("Not yet implemented")

    override suspend fun save(tokens: AuthTokens?) {
        TODO("Not yet implemented")
    }

    override suspend fun get(): AuthTokens? {
        TODO("Not yet implemented")
    }

}