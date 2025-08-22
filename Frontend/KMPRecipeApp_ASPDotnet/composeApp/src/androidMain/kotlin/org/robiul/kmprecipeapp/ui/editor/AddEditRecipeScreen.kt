package org.robiul.kmprecipeapp.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddEditRecipeScreen(mode: String = "add", id: String = "", onSaved: () -> Unit, onCancel: () -> Unit) {
    val title = remember { mutableStateOf("") }
    val instructions = remember { mutableStateOf("") }

    Scaffold(snackbarHost = { SnackbarHost(hostState = androidx.compose.material3.SnackbarHostState()) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text(text = if (mode == "add") "Add Recipe" else "Edit Recipe")
            OutlinedTextField(value = title.value, onValueChange = { title.value = it }, label = { Text("Title") }, modifier = Modifier.padding(top = 8.dp))
            OutlinedTextField(value = instructions.value, onValueChange = { instructions.value = it }, label = { Text("Instructions") }, modifier = Modifier.padding(top = 8.dp))
            Button(onClick = { /* TODO: call ViewModel -> create or update */ onSaved() }, modifier = Modifier.padding(top = 12.dp)) { Text("Save") }
            Button(onClick = onCancel, modifier = Modifier.padding(top = 8.dp)) { Text("Cancel") }
        }
    }
}
