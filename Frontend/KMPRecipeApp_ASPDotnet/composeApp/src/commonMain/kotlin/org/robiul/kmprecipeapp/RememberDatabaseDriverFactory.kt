package org.robiul.kmprecipeapp

import androidx.compose.runtime.Composable

/**
 * Return a platform-specific DriverFactory wrapped with remember so common code can call it
 */
@Composable
expect fun rememberDatabaseDriverFactory(): DriverFactory
