package org.robiul.kmprecipeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.robiul.kmprecipeapp.di.composeAppModule
import org.robiul.kmprecipeapp.navigation.AppNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(this@MainActivity)
            modules(composeAppModule)
        }

        setContent {
            AppNavGraph()
        }
    }
}
