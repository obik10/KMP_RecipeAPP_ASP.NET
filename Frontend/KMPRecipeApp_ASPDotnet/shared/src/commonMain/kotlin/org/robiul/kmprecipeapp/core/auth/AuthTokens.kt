// shared/src/commonMain/kotlin/org/robiul/kmprecipeapp/core/auth/AuthTokens.kt
package org.robiul.kmprecipeapp.core.auth

import org.robiul.kmprecipeapp.core.currentTimeMillis

data class AuthTokens(
    val access: String,
    val refresh: String,
    /** epoch millis when access token expires (with skew subtracted) */
    val expiresAtMillis: Long
) {
    /** Check if the access token has expired */
    fun isExpired(nowMillis: Long = currentTimeMillis()): Boolean = nowMillis >= expiresAtMillis
}
