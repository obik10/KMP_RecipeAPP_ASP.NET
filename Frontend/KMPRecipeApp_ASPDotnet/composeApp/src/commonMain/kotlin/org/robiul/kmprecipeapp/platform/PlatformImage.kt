// commonMain/src/.../platform/PlatformImage.kt
package org.robiul.kmprecipeapp.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
)
