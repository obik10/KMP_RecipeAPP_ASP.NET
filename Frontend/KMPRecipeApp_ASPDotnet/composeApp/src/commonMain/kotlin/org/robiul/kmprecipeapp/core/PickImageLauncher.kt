// commonMain
package org.robiul.kmprecipeapp.core

import androidx.compose.runtime.Composable

enum class ImageSource { Gallery, Camera }

@Composable
expect fun PickImageLauncher(
    onImagePicked: (fileName: String, bytes: ByteArray) -> Unit,
    sources: List<ImageSource> = listOf(ImageSource.Gallery, ImageSource.Camera),
    content: @Composable (onPick: (ImageSource) -> Unit) -> Unit
)
