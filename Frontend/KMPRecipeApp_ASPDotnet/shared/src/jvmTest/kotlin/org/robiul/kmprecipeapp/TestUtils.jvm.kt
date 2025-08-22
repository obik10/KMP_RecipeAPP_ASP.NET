package org.robiul.kmprecipeapp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

actual fun runTestCompat(block: suspend CoroutineScope.() -> Unit) {
    runBlocking(Dispatchers.Default + Job(), block)

}