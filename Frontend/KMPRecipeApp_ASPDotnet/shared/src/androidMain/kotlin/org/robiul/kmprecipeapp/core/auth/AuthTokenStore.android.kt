package org.robiul.kmprecipeapp.core.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

class AndroidAuthTokenStore(ctx: Context) : AuthTokenStore {
    private val master = MasterKey.Builder(ctx)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        ctx,
        "secure_tokens",
        master,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _tokens = MutableStateFlow(readFromDisk())
    override val tokens: StateFlow<AuthTokens?> = _tokens

    override suspend fun save(tokens: AuthTokens?) {
        if (tokens == null) {
            prefs.edit().clear().apply()
            _tokens.emit(null)
        } else {
            prefs.edit()
                .putString("a", tokens.access)
                .putString("r", tokens.refresh)
                .putLong("e", tokens.expiresAtMillis)
                .apply()
            _tokens.emit(tokens)
        }
    }

    override suspend fun get(): AuthTokens? = tokens.value

    private fun readFromDisk(): AuthTokens? {
        val a = prefs.getString("a", null) ?: return null
        val r = prefs.getString("r", null) ?: return null
        val e = prefs.getLong("e", 0L)
        return AuthTokens(a, r, e)
    }
}
