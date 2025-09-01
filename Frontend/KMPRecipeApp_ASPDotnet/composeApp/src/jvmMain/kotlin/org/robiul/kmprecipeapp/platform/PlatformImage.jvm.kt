// desktopMain/src/.../platform/PlatformImage.desktop.kt
package org.robiul.kmprecipeapp.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import kmprecipeapp_aspdotnet.composeapp.generated.resources.Res
import kmprecipeapp_aspdotnet.composeapp.generated.resources.placeholder

@Composable
actual fun PlatformImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier
) {
    Image(
        painter = painterResource(Res.drawable.placeholder),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
