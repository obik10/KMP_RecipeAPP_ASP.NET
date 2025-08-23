package org.robiul.kmprecipeapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.robiul.kmprecipeapp.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(onNavigateToLogin: () -> Unit = {}, onNavigateToRegister: () -> Unit = {}) {
    val vm: AuthViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val tokens by vm.tokens.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Top) {
        if (tokens == null) {
            Text("Not logged in", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) { Text("Login") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) { Text("Register") }
        } else {
            // If you have a user object expose it from a repository; fallback to email field from vm state
            Text("Logged in as", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.email.ifBlank { "(no email)" }, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
        }
    }
}
