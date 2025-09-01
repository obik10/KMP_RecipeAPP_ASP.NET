package org.robiul.kmprecipeapp.di

import org.robiul.kmprecipeapp.Constants
import org.robiul.kmprecipeapp.core.NetworkClient
import org.robiul.kmprecipeapp.core.auth.AuthTokenStore
import org.robiul.kmprecipeapp.core.auth.PersistentAuthTokenStore
import org.robiul.kmprecipeapp.core.createPlatformEngine
import org.robiul.kmprecipeapp.core.provideSettings
import org.robiul.kmprecipeapp.data.datasource.LocalDataSource
import org.robiul.kmprecipeapp.data.datasource.RemoteDataSource
import org.robiul.kmprecipeapp.data.repository.AuthRepositoryImpl
import org.robiul.kmprecipeapp.data.repository.RecipeRepositoryImpl
import org.robiul.kmprecipeapp.domain.repository.AuthRepository
import org.robiul.kmprecipeapp.domain.usecase.*
import org.robiul.kmprecipeapp.presentation.viewmodel.*
import app.cash.sqldelight.db.SqlDriver
import com.russhwolf.settings.Settings

object AppModule {

    private lateinit var driver: SqlDriver

    // ---- Shared token store ----
    val tokenStore: AuthTokenStore by lazy { PersistentAuthTokenStore(provideSettings()) }

    // must be called before anything else
    fun init(driver: SqlDriver) {
        this.driver = driver
    }

    // ---- Network client ----
    private val apiClient by lazy {
        NetworkClient(
            baseUrl = Constants.BASE_URL_API,
            engine = createPlatformEngine(),
            tokenStore = tokenStore
        )
    }

    // ---- Data sources ----
    private val localDataSource: LocalDataSource by lazy {
        check(::driver.isInitialized) { "AppModule not initialized! Call AppModule.init(driver) first." }
        LocalDataSource(driver)
    }

    private val remoteDataSource: RemoteDataSource by lazy {
        RemoteDataSource(apiClient)
    }

    // ---- Repositories ----
    private val recipeRepository by lazy {
        RecipeRepositoryImpl(remote = remoteDataSource, local = localDataSource)
    }

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            remote = remoteDataSource,
            tokenStore = tokenStore,
            keycloakUrl = Constants.BASE_URL_KEYCLOAK
        )
    }

    // ---- Use cases ----
    val getRecipesUseCase by lazy { GetRecipesPaginated(recipeRepository) }
    val searchRecipesUseCase by lazy { SearchRecipes(recipeRepository) }
    val getRecipeByIdUseCase by lazy { GetRecipeById(recipeRepository) }
    val createRecipeUseCase by lazy { CreateRecipe(recipeRepository) }
    val updateRecipeUseCase by lazy { UpdateRecipe(recipeRepository) }
    val deleteRecipeUseCase by lazy { DeleteRecipe(recipeRepository) }
    val uploadRecipeImageUseCase by lazy { UploadRecipeImage(recipeRepository) }
    val getMyRecipesUseCase by lazy { GetMyRecipes(recipeRepository) }



    // Favorites
    val getMyFavoritesUseCase by lazy { GetMyFavorites(recipeRepository) }
    val addFavoriteUseCase by lazy { AddFavorite(recipeRepository) }
    val removeFavoriteUseCase by lazy { RemoveFavorite(recipeRepository) }


    // ---- Expose repositories ----
    fun provideAuthRepository(): AuthRepository = authRepository
    fun provideRecipeRepository(): RecipeRepositoryImpl = recipeRepository

    // ---- ViewModels ----
    fun provideHomeViewModel(): HomeViewModel =
        HomeViewModel(getRecipesUseCase, searchRecipesUseCase)

    fun provideProfileViewModel(): ProfileViewModel =
        ProfileViewModel(authRepository)

    fun provideLoginViewModel(): LoginViewModel =
        LoginViewModel(authRepository)

    fun provideRegisterViewModel(): RegisterViewModel {
        val repo = provideAuthRepository()
        val useCase = RegisterUser(repo)
        return RegisterViewModel(useCase)
    }


    private val settings: Settings by lazy { provideSettings() }

    fun provideFavoritesViewModel(): FavoritesViewModel =
        FavoritesViewModel(recipeRepository, settings)

    fun provideMyRecipesViewModel(): MyRecipesViewModel {
        val getMyRecipes = GetMyRecipes(provideRecipeRepository())
        return MyRecipesViewModel(getMyRecipes)
    }



}
