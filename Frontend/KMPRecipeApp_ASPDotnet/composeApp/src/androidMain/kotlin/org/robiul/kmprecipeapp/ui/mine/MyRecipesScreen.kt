package org.robiul.kmprecipeapp.ui.mine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MyRecipesScreen(onOpen: (String) -> Unit, onBack: () -> Unit) {
    val items = remember { List(5) { "my-recipe-${it + 1}" } }
    Scaffold(snackbarHost = { SnackbarHost(hostState = androidx.compose.material3.SnackbarHostState()) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(items) { id ->
                Column(modifier = Modifier.clickable { onOpen(id) }.padding(16.dp)) {
                    Text(text = "My Recipe $id")
                }
            }
        }
    }
}
