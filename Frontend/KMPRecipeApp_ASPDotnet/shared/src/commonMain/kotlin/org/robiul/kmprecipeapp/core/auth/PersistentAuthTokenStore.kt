package org.robiul.kmprecipeapp.core.auth

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import org.robiul.kmprecipeapp.utils.decodeJwtPayload

/**
 * Persists tokens across app restarts using multiplatform-settings.
 */
class PersistentAuthTokenStore(
    private val settings: Settings
) : AuthTokenStore {

    private val _tokens = MutableStateFlow<AuthTokens?>(loadFromStorage())
    override val tokens: StateFlow<AuthTokens?> = _tokens

    override suspend fun save(tokens: AuthTokens?) {
        if (tokens == null) {
            clear()
            return
        }

        println("🟢 [PersistentTokenStore.save] access=${tokens.access.take(30)} refresh=${tokens.refresh.take(30)}")

        // Save to in-memory
        _tokens.emit(tokens)

        // Save to persistent storage
        settings["access_token"] = tokens.access
        settings["refresh_token"] = tokens.refresh
        settings["expires_at"] = tokens.expiresAtMillis.toString()

        tokens.access.let { access ->
            val decoded = decodeJwtPayload(access)
            println("🔐 [JWT Payload] $decoded")
        }
    }

    override suspend fun get(): AuthTokens? {
        val token = tokens.first()
        println("🔵 [PersistentTokenStore.get] access=${token?.access?.take(30)}")
        return token
    }

    override suspend fun clear() {
        println("🟡 [PersistentTokenStore.clear] Tokens cleared")
        _tokens.emit(null)

        settings.remove("access_token")
        settings.remove("refresh_token")
        settings.remove("expires_at")
    }

    /**
     * Load from persistent storage at startup.
     */
    private fun loadFromStorage(): AuthTokens? {
        val access = settings.getStringOrNull("access_token") ?: return null
        val refresh = settings.getStringOrNull("refresh_token") ?: ""
        val expiresAt = settings.getStringOrNull("expires_at")?.toLongOrNull() ?: 0L

        return AuthTokens(
            access = access,
            refresh = refresh,
            expiresAtMillis = expiresAt
        )
    }

    // Helper extensions for Settings
    private fun Settings.getStringOrNull(key: String): String? =
        if (hasKey(key)) this[key] else null
}
