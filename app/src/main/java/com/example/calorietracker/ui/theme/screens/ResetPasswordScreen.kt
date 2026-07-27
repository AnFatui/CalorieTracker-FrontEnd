package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.ResetPasswordViewModel

@Composable
fun ResetPasswordScreen(
    onPasswordUpdated: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: ResetPasswordViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { onShowMessage(it) }
    }

    val canSubmit = password.isNotBlank() && password == passwordRepeat

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Neues Passwort setzen",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Vergib ein neues Passwort für dein Konto.",
            color = Color.Gray,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(38.dp))

        ResetPasswordInput("Neues Passwort", password) { password = it }
        Spacer(Modifier.height(14.dp))
        ResetPasswordInput("Passwort bestätigen", passwordRepeat) { passwordRepeat = it }

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

        if (uiState.loading) {
            CircularProgressIndicator(color = Color(0xFF22C55E))
        } else {
            PrimaryButton(
                text = "Passwort speichern",
                onClick = {
                    viewModel.updatePassword(password, onPasswordUpdated)
                },
                enabled = canSubmit
            )
        }
    }
}

@Composable
private fun ResetPasswordInput(
    text: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text, color = Color.Gray, fontSize = 13.sp) },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
