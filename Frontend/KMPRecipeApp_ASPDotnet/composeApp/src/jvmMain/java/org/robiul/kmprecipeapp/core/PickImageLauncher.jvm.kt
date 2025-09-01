package org.robiul.kmprecipeapp.core

import androidx.compose.runtime.Composable

@Composable
actual fun PickImageLauncher(
    onImagePicked: (String, ByteArray) -> Unit,
    content: @Composable (() -> Unit)
) {
}