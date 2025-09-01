package org.robiul.kmprecipeapp

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberDatabaseDriverFactory(): DriverFactory {
    val context: Context = LocalContext.current
    return remember { DriverFactory(context) } // assumes android actual DriverFactory(context)
}
