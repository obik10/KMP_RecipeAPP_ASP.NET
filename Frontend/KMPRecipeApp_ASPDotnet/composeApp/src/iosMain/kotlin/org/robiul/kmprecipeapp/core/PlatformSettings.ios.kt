package org.robiul.kmprecipeapp.core

import com.russhwolf.settings.AppleSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual fun provideSettings(): Settings {
    return AppleSettings(NSUserDefaults.standardUserDefaults)
}
