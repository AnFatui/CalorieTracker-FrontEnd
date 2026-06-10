package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.theme.components.PrimaryButton

@Composable
fun RegisterScreen(
    onCreateAccountClick: () -> Unit,
    onBackToLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(70.dp))

        Text("Account erstellen", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Starte mit deinem persönlichen Tracking.", color = Color.Gray, fontSize = 13.sp)

        Spacer(Modifier.height(45.dp))

        RegisterInput(
            text = "Name",
            value = name,
            keyboardType = KeyboardType.Text,
            onValueChange = { name = it }
        )

        Spacer(Modifier.height(14.dp))

        RegisterInput(
            text = "E-Mail",
            value = email,
            keyboardType = KeyboardType.Email,
            onValueChange = { email = it }
        )

        Spacer(Modifier.height(14.dp))

        RegisterInput(
            text = "Passwort",
            value = password,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            onValueChange = { password = it }
        )

        Spacer(Modifier.height(14.dp))

        RegisterInput(
            text = "Passwort bestätigen",
            value = passwordRepeat,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            onValueChange = { passwordRepeat = it }
        )

        Spacer(Modifier.height(36.dp))

        PrimaryButton("Account erstellen", onCreateAccountClick)

        Spacer(Modifier.height(28.dp))

        Row {
            Text("Schon einen Account? ", color = Color.White, fontSize = 12.sp)

            Text(
                "Einloggen",
                color = Color(0xFF22C55E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackToLoginClick() }
            )
        }
    }
}

@Composable
private fun RegisterInput(
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
            .height(54.dp),
        shape = RoundedCornerShape(22.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1F2937),
            unfocusedContainerColor = Color(0xFF1F2937),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White
        ),
        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
    )
}