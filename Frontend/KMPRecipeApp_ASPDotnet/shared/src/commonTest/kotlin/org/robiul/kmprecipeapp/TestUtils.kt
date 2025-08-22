package org.robiul.kmprecipeapp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * A small helper to run suspend tests across KMP targets without bringing in extra test libs.
 */
expect fun runTestCompat(block: suspend CoroutineScope.() -> Unit)

