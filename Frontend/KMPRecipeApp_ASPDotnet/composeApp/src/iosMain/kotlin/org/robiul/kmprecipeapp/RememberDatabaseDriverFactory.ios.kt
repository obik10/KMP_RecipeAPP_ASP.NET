package org.robiul.kmprecipeapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberDatabaseDriverFactory(): DriverFactory {
    return remember { DriverFactory() } // assumes ios actual DriverFactory() has no args
}
