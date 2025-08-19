package org.robiul.kmprecipeapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KMPRecipeApp_ASPDotnet",
    ) {
        App()
    }
}