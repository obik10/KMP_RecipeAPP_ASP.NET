package org.robiul.kmprecipeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.robiul.kmprecipeapp.core.ImageSource
import org.robiul.kmprecipeapp.core.PickImageLauncher
import org.robiul.kmprecipeapp.presentation.viewmodel.MyRecipesAddViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecipesAddScreen(
    viewModel: MyRecipesAddViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }

    var ingredients by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var previewBytes by remember { mutableStateOf<ByteArray?>(null) }

    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val successMessage = viewModel.successMessage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        PickImageLauncher(
            sources = listOf(ImageSource.Gallery, ImageSource.Camera),
            onImagePicked = { fileName, bytes ->
                previewBytes = bytes
                viewModel.setImage(fileName, bytes)
            }
        ) { pickImage ->

            LazyColumn(
                contentPadding = padding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // --- Image picker + preview ---
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { pickImage(ImageSource.Gallery) },
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(4.dp)
                            ) {
                                Text("Gallery")
                            }
                            Button(
                                onClick = { pickImage(ImageSource.Camera) },
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(4.dp)
                            ) {
                                Text("Camera")
                            }
                        }

                        previewBytes?.let { bytes ->
                            Spacer(Modifier.height(12.dp))
                            AsyncImage(
                                model = bytes,
                                contentDescription = "Recipe Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                    }
                }

                // --- Title ---
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // --- Instructions ---
                item {
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instructions") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // --- Ingredients ---
                item { Text("Ingredients", style = MaterialTheme.typography.titleMedium) }

                itemsIndexed(ingredients) { index, (name, measure) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { newName ->
                                ingredients = ingredients.toMutableList().also {
                                    it[index] = newName to measure
                                }
                            },
                            label = { Text("Name") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = measure,
                            onValueChange = { newMeasure ->
                                ingredients = ingredients.toMutableList().also {
                                    it[index] = name to newMeasure
                                }
                            },
                            label = { Text("Measure") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        IconButton(onClick = {
                            ingredients = ingredients.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                }

                // Add ingredient button
                item {
                    Button(
                        onClick = { ingredients = ingredients + ("" to "") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
                        Spacer(Modifier.width(8.dp))
                        Text("Add Ingredient")
                    }
                }

                // --- YouTube URL ---
                item {
                    OutlinedTextField(
                        value = youtubeUrl,
                        onValueChange = { youtubeUrl = it },
                        label = { Text("YouTube URL (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // --- Save button ---
                item {
                    Button(
                        onClick = {
                            viewModel.saveRecipe(
                                title = title,
                                instructions = instructions,
                                youtubeUrl = youtubeUrl.ifBlank { null },
                                ingredients = ingredients.map { (name, measure) ->
                                    org.robiul.kmprecipeapp.domain.models.Ingredient(
                                        name = name,
                                        measure = measure
                                    )
                                },
                                onSaved = onBack
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(6.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Recipe")
                        }
                    }
                }

                // --- Error / success messages ---
                if (errorMessage != null) {
                    item {
                        Text(
                            "Error: $errorMessage",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                if (successMessage != null) {
                    item {
                        Text(
                            successMessage,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
