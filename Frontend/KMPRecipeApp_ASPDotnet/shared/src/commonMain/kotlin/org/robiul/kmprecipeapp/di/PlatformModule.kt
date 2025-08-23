package org.robiul.kmprecipeapp.di

import io.ktor.client.engine.HttpClientEngine
import org.koin.dsl.module
import org.robiul.kmprecipeapp.core.createPlatformEngine
import org.robiul.kmprecipeapp.core.auth.InMemoryAuthTokenStore
import org.robiul.kmprecipeapp.core.auth.AuthTokenStore

val platformModule = module {
    single<HttpClientEngine> { createPlatformEngine() }
    single<AuthTokenStore> { InMemoryAuthTokenStore() }
}
