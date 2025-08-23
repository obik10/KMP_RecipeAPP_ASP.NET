package org.robiul.kmprecipeapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.android.get
import org.koin.core.context.startKoin
import org.robiul.kmprecipeapp.di.androidPlatformModule
import org.robiul.kmprecipeapp.di.composeAppModule
import org.robiul.kmprecipeapp.di.databaseModule
import org.robiul.kmprecipeapp.di.initKoin
import org.robiul.kmprecipeapp.di.networkModule
import org.robiul.kmprecipeapp.di.platformModule
import org.robiul.kmprecipeapp.di.repositoryModule
import org.robiul.kmprecipeapp.di.useCaseModule
import org.robiul.kmprecipeapp.navigation.AppNavGraph
import org.robiul.kmprecipeapp.domain.repository.AuthRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Koin
        startKoin {
            androidContext(this@MainActivity)
            modules(
                platformModule,
                networkModule,
                databaseModule,
                repositoryModule,
                useCaseModule,
                androidPlatformModule,
                composeAppModule
            )
        }

        // Quick test for AuthRepository
        val authRepo: AuthRepository = get()
        lifecycleScope.launch { quickLoginTest(authRepo) }

        setContent { AppNavGraph() }
    }
}

// Suspend function to quickly test login, token, refresh
suspend fun quickLoginTest(repo: AuthRepository) {
    val tag = "QuickLoginTest"

    val loginResult = repo.login(Constants.TEST_USERNAME, Constants.TEST_PASSWORD)
    Log.d(tag, "Login result: $loginResult")

    val currentToken = repo.currentToken()
    Log.d(tag, "Current token: $currentToken")

    val refreshToken = repo.refreshToken()
    Log.d(tag, "Refresh token: $refreshToken")
}
