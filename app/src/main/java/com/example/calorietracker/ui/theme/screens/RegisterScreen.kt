package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    onCreateAccountSuccess: (String) -> Unit,
    onBackToLoginClick: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: RegisterViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            onShowMessage(it)
        }
    }

    val canCreateAccount =
        name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && password == passwordRepeat

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Account erstellen",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text("Starte mit deinem persönlichen Tracking.", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(38.dp))

        RegisterInput("Name", name, KeyboardType.Text) { name = it.trim() }
        Spacer(Modifier.height(14.dp))
        RegisterInput("E-Mail", email, KeyboardType.Email) { email = it.trim() }
        Spacer(Modifier.height(14.dp))
        RegisterInput("Passwort", password, KeyboardType.Password, true) { password = it }
        Spacer(Modifier.height(14.dp))
        RegisterInput(
            "Passwort bestätigen",
            passwordRepeat,
            KeyboardType.Password,
            true
        ) { passwordRepeat = it }

        if (passwordRepeat.isNotBlank() && password != passwordRepeat) {
            Text(
                "Passwörter stimmen nicht überein.",
                color = Color(0xFFEF4444),
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = Color(0xFFEF4444),
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        if (uiState.loading) {
            CircularProgressIndicator(color = Color(0xFF22C55E))
        } else {
            PrimaryButton(
                text = "Account erstellen",
                onClick = {
                    viewModel.signUp(name, email, password, onCreateAccountSuccess)
                },
                enabled = canCreateAccount
            )
        }

        Spacer(Modifier.height(26.dp))
        Row {
            Text("Schon einen Account? ", color = Color.White, fontSize = 12.sp)
            Text(
                "Einloggen",
                color = Color(0xFF22C55E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackToLoginClick() })
        }
    }
}

@Composable
private fun RegisterInput(
    text: String,
    value: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text, color = Color.Gray, fontSize = 13.sp) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(22.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1F2937),
            unfocusedContainerColor = Color(0xFF1F2937),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White
        ),
        textStyle = TextStyle(color = Color.White)
    )
}
