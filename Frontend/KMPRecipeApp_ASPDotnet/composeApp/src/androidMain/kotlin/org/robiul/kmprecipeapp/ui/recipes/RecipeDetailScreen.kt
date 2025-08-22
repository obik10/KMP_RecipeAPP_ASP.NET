package org.robiul.kmprecipeapp.ui.recipes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecipeDetailScreen(id: String, onEdit: () -> Unit, onBack: () -> Unit) {
    Scaffold(snackbarHost = { SnackbarHost(hostState = androidx.compose.material3.SnackbarHostState()) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text(text = "Recipe Detail: $id")
            Text(text = "Full instructions go here...", modifier = Modifier.padding(top = 8.dp))
            Button(onClick = onEdit, modifier = Modifier.padding(top = 12.dp)) { Text("Edit") }
            Button(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("Back") }
        }
    }
}