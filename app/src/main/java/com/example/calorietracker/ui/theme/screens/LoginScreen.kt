package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: LoginViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Observe error state and show snackbar via the activity's callback
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            onShowMessage(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🔥", fontSize = 44.sp)
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "CalorieTracker",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Track your goals",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )

        Spacer(Modifier.height(50.dp))

        LoginInputField(
            text = "E-Mail",
            value = email,
            keyboardType = KeyboardType.Email,
            onValueChange = { email = it }
        )

        Spacer(Modifier.height(16.dp))

        LoginInputField(
            text = "Passwort",
            value = password,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            onValueChange = { password = it }
        )

        Text(
            text = "Passwort vergessen?",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 10.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp)
                .clickable {
                    showForgotPasswordDialog = true
                },
            textAlign = TextAlign.End
        )

        if (uiState.error != null) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = uiState.error!!,
                color = Color(0xFFF87171),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        } else {
            Spacer(Modifier.height(46.dp))
        }

        if (uiState.loading) {
            CircularProgressIndicator(color = Color(0xFF22C55E))
        } else {
            PrimaryButton(
                text = "Einloggen",
                onClick = {
                    viewModel.login(email, password, onLoginSuccess)
                },
                enabled = email.isNotBlank() && password.isNotBlank()
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "oder weitermachen mit",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )

        Spacer(Modifier.height(22.dp))

        Box(
            modifier = Modifier
                .width(190.dp)
                .height(54.dp)
                .border(1.dp, Color(0xFF4B5563), RoundedCornerShape(18.dp))
                .clickable {
                    // Google Login wird später angebunden.
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "G   Google",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.weight(1f))

        Row(modifier = Modifier.padding(bottom = 32.dp)) {
            Text("Noch keinen Account? ", color = Color.White, fontSize = 12.sp)

            Text(
                "Registrieren",
                color = Color(0xFF22C55E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
            },
            containerColor = Color(0xFF1F2937),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f),
            title = {
                Text("Passwort zurücksetzen")
            },
            text = {
                Text("Diese Funktion wird später ergänzt.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showForgotPasswordDialog = false
                    }
                ) {
                    Text("OK", color = Color(0xFF22C55E))
                }
            }
        )
    }
}

@Composable
private fun LoginInputField(
    text: String,
    value: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType,
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
