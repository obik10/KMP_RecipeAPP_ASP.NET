package org.robiul.kmprecipeapp

import kotlinx.coroutines.CoroutineScope

actual fun runTestCompat(block: suspend CoroutineScope.() -> Unit) {
}