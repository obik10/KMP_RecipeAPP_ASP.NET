package org.robiul.kmprecipeapp.core

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun createPlatformEngine(): HttpClientEngine = Js.create()
