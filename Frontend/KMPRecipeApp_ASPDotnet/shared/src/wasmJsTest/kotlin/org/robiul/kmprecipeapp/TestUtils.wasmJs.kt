package org.robiul.kmprecipeapp

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise

actual fun runTestCompat(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) {
    MainScope().promise { block() }

}