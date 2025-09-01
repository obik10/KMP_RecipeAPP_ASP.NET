package org.robiul.kmprecipeapp.utils

import io.ktor.utils.io.charsets.Charsets
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
fun decodeJwtPayload(jwt: String): Map<String, Any?>? {
    return try {
        val parts = jwt.split(".")
        if (parts.size < 2) return null

        // ✅ Works in KMP
        val payload = Base64.decode(parts[1]).decodeToString()

        val jsonElement = Json { ignoreUnknownKeys = true }.parseToJsonElement(payload)
        if (jsonElement is JsonObject) {
            jsonElement.mapValues { it.value.jsonPrimitive.contentOrNull }
        } else null
    } catch (e: Exception) {
        null
    }
}
