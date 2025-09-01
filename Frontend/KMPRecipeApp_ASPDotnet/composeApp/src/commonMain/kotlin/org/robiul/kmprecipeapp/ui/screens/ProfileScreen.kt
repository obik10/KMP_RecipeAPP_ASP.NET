package org.robiul.kmprecipeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    isLoggedIn: Boolean,
    onLogout: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoggedIn) {
            // ✅ Logged in state (no username/email needed)
            Text(
                text = "You are logged in ✅",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Logout")
            }
        } else {
            // ❌ Not logged in state
            Text(
                text = "Not logged in",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Register")
            }
        }
    }
}
