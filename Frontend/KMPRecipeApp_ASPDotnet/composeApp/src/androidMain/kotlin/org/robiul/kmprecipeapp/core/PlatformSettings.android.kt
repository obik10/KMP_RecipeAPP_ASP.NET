package org.robiul.kmprecipeapp.core

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

lateinit var appContext: Context

fun initSettings(context: Context) {
    appContext = context.applicationContext
}

actual fun provideSettings(): Settings {
    val sharedPrefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(sharedPrefs)
}
