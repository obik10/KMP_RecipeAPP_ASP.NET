package org.robiul.kmprecipeapp

import kotlinx.coroutines.CoroutineScope

actual fun runTestCompat(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) {
}
