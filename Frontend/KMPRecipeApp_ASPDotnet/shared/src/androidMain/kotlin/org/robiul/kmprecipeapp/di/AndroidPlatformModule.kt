package org.robiul.kmprecipeapp.di

import android.content.Context
import org.koin.dsl.module
import org.robiul.kmprecipeapp.core.auth.AuthTokenStore
import org.robiul.kmprecipeapp.core.auth.AndroidAuthTokenStore

val androidPlatformModule = module {
//    single<Context> { get() }
    single<AuthTokenStore> { AndroidAuthTokenStore(get()) }
}
