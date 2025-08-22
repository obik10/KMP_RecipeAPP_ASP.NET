package org.robiul.kmprecipeapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

actual fun runTestCompat(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) {
    runBlocking(Dispatchers.Default) { block() }
}