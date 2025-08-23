//package org.robiul.kmprecipeapp.ui.register
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun RegisterScreen(onRegisterSuccess: () -> Unit, onCancel: () -> Unit) {
//    val username = remember { mutableStateOf("") }
//    val email = remember { mutableStateOf("") }
//    val password = remember { mutableStateOf("") }
//
//    Scaffold(snackbarHost = { SnackbarHost(hostState = androidx.compose.material3.SnackbarHostState()) }) { padding ->
//        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
//            Text(text = "Register")
//            OutlinedTextField(value = username.value, onValueChange = { username.value = it }, label = { Text("Username") }, modifier = Modifier.padding(top = 8.dp))
//            OutlinedTextField(value = email.value, onValueChange = { email.value = it }, label = { Text("Email") }, modifier = Modifier.padding(top = 8.dp))
//            OutlinedTextField(value = password.value, onValueChange = { password.value = it }, label = { Text("Password") }, modifier = Modifier.padding(top = 8.dp))
//            Button(onClick = { /* TODO: call ViewModel */ onRegisterSuccess() }, modifier = Modifier.padding(top = 12.dp)) { Text("Create") }
//            Button(onClick = onCancel, modifier = Modifier.padding(top = 8.dp)) { Text("Cancel") }
//        }
//    }
//}