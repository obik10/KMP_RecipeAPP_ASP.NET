package org.robiul.kmprecipeapp.di

import app.cash.sqldelight.db.SqlDriver
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.robiul.kmprecipeapp.Constants
import org.robiul.kmprecipeapp.core.NetworkClient
import org.robiul.kmprecipeapp.core.createPlatformEngine
import org.robiul.kmprecipeapp.core.auth.AuthTokenStore
import org.robiul.kmprecipeapp.core.auth.InMemoryAuthTokenStore
import org.robiul.kmprecipeapp.data.datasource.LocalDataSource
import org.robiul.kmprecipeapp.data.datasource.RemoteDataSource
import org.robiul.kmprecipeapp.data.repository.AuthRepositoryImpl
import org.robiul.kmprecipeapp.data.repository.RecipeRepositoryImpl
import org.robiul.kmprecipeapp.db.AppDatabase
import org.robiul.kmprecipeapp.db.DriverFactory
import org.robiul.kmprecipeapp.domain.repository.AuthRepository
import org.robiul.kmprecipeapp.domain.repository.RecipeRepository
import org.robiul.kmprecipeapp.domain.usecase.AddFavorite
import org.robiul.kmprecipeapp.domain.usecase.CreateRecipe
import org.robiul.kmprecipeapp.domain.usecase.DeleteRecipe
import org.robiul.kmprecipeapp.domain.usecase.GetMyFavorites
import org.robiul.kmprecipeapp.domain.usecase.GetMyRecipes
import org.robiul.kmprecipeapp.domain.usecase.GetRecipeById
import org.robiul.kmprecipeapp.domain.usecase.GetRecipesPaginated
import org.robiul.kmprecipeapp.domain.usecase.RegisterUser
import org.robiul.kmprecipeapp.domain.usecase.RemoveFavorite
import org.robiul.kmprecipeapp.domain.usecase.SearchRecipes
import org.robiul.kmprecipeapp.domain.usecase.UpdateRecipe
import org.robiul.kmprecipeapp.domain.usecase.UploadRecipeImage

data class NetworkConfig(val baseUrl: String = Constants.DEFAULT_BASE_URL)

val platformModule: Module = module {
    // Default engine + in-memory token store; override in Android/iOS app if needed
    single<HttpClientEngine> { createPlatformEngine() }
    single<AuthTokenStore> { InMemoryAuthTokenStore() }
}

val networkModule: Module = module {
    single { NetworkConfig() }
    single { NetworkClient(get<NetworkConfig>().baseUrl, get(), get()) }
    single { RemoteDataSource(get()) }
}

val databaseModule: Module = module {
    single<SqlDriver> { get<DriverFactory>().createDriver() }
    single { AppDatabase(get()) }
    single { LocalDataSource(get()) }
}

val repositoryModule: Module = module {
    single { AuthRepositoryImpl(get(), get()) } bind AuthRepository::class
    single { RecipeRepositoryImpl(get(), get()) } bind RecipeRepository::class
}

val useCaseModule: Module = module {
    single { RegisterUser(get()) }
    single { GetRecipesPaginated(get()) }
    single { SearchRecipes(get()) }
    single { GetRecipeById(get()) }

    single { CreateRecipe(get()) }
    single { UpdateRecipe(get()) }
    single { DeleteRecipe(get()) }
    single { UploadRecipeImage(get()) }
    single { GetMyRecipes(get()) }
    single { AddFavorite(get()) }
    single { RemoveFavorite(get()) }
    single { GetMyFavorites(get()) }
}

fun initKoin(vararg extra: Module) = startKoin {
    modules(platformModule, networkModule, databaseModule, repositoryModule, useCaseModule, *extra)
}
