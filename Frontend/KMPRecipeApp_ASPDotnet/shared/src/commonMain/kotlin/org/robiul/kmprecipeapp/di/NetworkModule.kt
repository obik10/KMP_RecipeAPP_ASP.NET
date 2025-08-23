package org.robiul.kmprecipeapp.di

import org.koin.dsl.module
import org.robiul.kmprecipeapp.Constants
import org.robiul.kmprecipeapp.core.NetworkClient
import org.robiul.kmprecipeapp.core.NetworkConfig
import org.robiul.kmprecipeapp.data.datasource.RemoteDataSource

val networkModule = module {
    single { NetworkConfig() }

    // API NetworkClient — explicitly point to API base URL (emulator loopback)
    single {
        NetworkClient(
            baseUrl = Constants.BASE_URL_API,
            engine = get(),
            tokenStore = get()
        )
    }

    single { RemoteDataSource(get()) } // uses NetworkClient (API)
}
