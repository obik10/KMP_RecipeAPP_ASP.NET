package org.robiul.kmprecipeapp.di

import org.koin.dsl.module
import org.robiul.kmprecipeapp.core.NetworkConfig
import org.robiul.kmprecipeapp.data.repository.AuthRepositoryImpl
import org.robiul.kmprecipeapp.domain.repository.AuthRepository
import org.robiul.kmprecipeapp.data.repository.RecipeRepositoryImpl
import org.robiul.kmprecipeapp.domain.repository.RecipeRepository

val repositoryModule = module {
    single<AuthRepository> {
        AuthRepositoryImpl(
            remote = get(),                 // RemoteDataSource
            tokenStore = get(),             // AuthTokenStore
            keycloakUrl = get<NetworkConfig>().baseUrl
        )
    }

    single<RecipeRepository> { RecipeRepositoryImpl(get(), get()) }
}
