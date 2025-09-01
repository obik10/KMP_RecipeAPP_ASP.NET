// androidMain/src/.../platform/PlatformImage.android.kt
package org.robiul.kmprecipeapp.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import kmprecipeapp_aspdotnet.composeapp.generated.resources.Res
import androidx.compose.foundation.Image
import coil3.request.crossfade
import kmprecipeapp_aspdotnet.composeapp.generated.resources.placeholder
import androidx.compose.ui.res.painterResource as androidPainterResource

@Composable
actual fun PlatformImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier
) {
    val imageUrl = url?.takeIf { it.isNotBlank() }

    println("PlatformImage load: $imageUrl")

    // Use Coil's AsyncImage
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        placeholder = painterResource(Res.drawable.placeholder),
        error = painterResource(Res.drawable.placeholder),
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}
