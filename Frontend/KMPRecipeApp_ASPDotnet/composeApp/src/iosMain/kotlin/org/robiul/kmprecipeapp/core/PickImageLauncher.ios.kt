package org.robiul.kmprecipeapp.core

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
actual fun PickImageLauncher(
    onImagePicked: (fileName: String, bytes: ByteArray) -> Unit,
    content: @Composable () -> Unit
) {
    // TODO: Implement UIImagePickerController
    Button(onClick = { /* not implemented yet */ }) {
        Text("Pick Image (iOS not implemented)")
    }
}