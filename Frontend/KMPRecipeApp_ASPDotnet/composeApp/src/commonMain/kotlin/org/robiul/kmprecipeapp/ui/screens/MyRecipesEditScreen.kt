package org.robiul.kmprecipeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.robiul.kmprecipeapp.core.ImageSource
import org.robiul.kmprecipeapp.core.PickImageLauncher
import org.robiul.kmprecipeapp.presentation.viewmodel.MyRecipesEditViewModel
import org.robiul.kmprecipeapp.ui.components.IngredientEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecipesEditScreen(
    viewModel: MyRecipesEditViewModel,
    onBack: () -> Unit,
    onPickImage: ((fileName: String, bytes: ByteArray) -> Unit)? = null
) {
    LaunchedEffect(Unit) { viewModel.loadRecipe() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(36.dp))
                viewModel.errorMessage?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
                viewModel.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            }

            item {
                viewModel.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Recipe image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = viewModel.imageUrl.orEmpty(),
                    onValueChange = { /* read-only */ },
                    label = { Text("Image URL (read-only)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )
            }

            item {
                PickImageLauncher(
                    onImagePicked = { fileName, bytes ->
                        viewModel.uploadImage(fileName, bytes)
                    }
                ) { onPick ->
                    Row {
                        Button(onClick = { onPick(ImageSource.Gallery) }) {
                            Text("Pick from Gallery")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { onPick(ImageSource.Camera) }) {
                            Text("Take Photo")
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = viewModel.title,
                    onValueChange = { viewModel.title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { viewModel.addIngredient() }) { Text("Add Ingredient") }
                }
            }

            itemsIndexed(
                items = viewModel.ingredients,
                key = { _, ing -> ing.id }
            ) { index, ing ->
                IngredientEditor(
                    ingredient = ing,
                    onChange = { updated -> viewModel.updateIngredient(index, updated) },
                    onRemove = { viewModel.removeIngredient(index) }
                )
            }

            item {
                OutlinedTextField(
                    value = viewModel.instructions,
                    onValueChange = { viewModel.instructions = it },
                    label = { Text("Instructions") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 10
                )
            }

            item {
                OutlinedTextField(
                    value = viewModel.youtubeUrl,
                    onValueChange = { viewModel.youtubeUrl = it },
                    label = { Text("YouTube URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.updateRecipe(onBack) },
                        enabled = !viewModel.isLoading
                    ) { Text("Update") }

                    OutlinedButton(
                        onClick = { viewModel.deleteRecipe(onBack) },
                        enabled = !viewModel.isLoading
                    ) { Text("Delete") }
                }
            }
        }
    }
}
