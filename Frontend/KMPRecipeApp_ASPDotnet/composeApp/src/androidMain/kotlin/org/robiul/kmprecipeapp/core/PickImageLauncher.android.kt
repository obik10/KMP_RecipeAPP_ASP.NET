// androidMain
package org.robiul.kmprecipeapp.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.InputStream
import java.util.*

@Composable
actual fun PickImageLauncher(
    onImagePicked: (fileName: String, bytes: ByteArray) -> Unit,
    sources: List<ImageSource>,
    content: @Composable (onPick: (ImageSource) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val bytes = context.readBytes(uri) ?: return@rememberLauncherForActivityResult
        val name = context.getFileName(uri)  // ✅ real filename
        onImagePicked(name, bytes)
    }


    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                val bytes = context.readBytes(uri) ?: return@let
                val name = "camera_${UUID.randomUUID()}.jpg"
                onImagePicked(name, bytes)
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = context.createTempImageUri()
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    content { source ->
        when (source) {
            ImageSource.Gallery -> galleryLauncher.launch("image/*")
            ImageSource.Camera -> {
                when {
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        val uri = context.createTempImageUri()
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            }
        }
    }
}

private fun Context.readBytes(uri: Uri): ByteArray? =
    contentResolver.openInputStream(uri)?.use(InputStream::readBytes)

private fun Context.createTempImageUri(): Uri {
    val file = File.createTempFile(
        "camera_", ".jpg",
        getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    )
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
}

private fun Context.getFileName(uri: Uri): String {
    var name = "gallery_${UUID.randomUUID()}.jpg"
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex != -1) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}