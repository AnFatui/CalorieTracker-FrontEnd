package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.theme.components.PrimaryButton

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(70.dp))

        Box(
            modifier = Modifier
                .size(92.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🔥", fontSize = 42.sp)
        }

        Text(
            text = "Name der App",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(55.dp))

        LoginInputField(
            text = "E-Mail",
            value = email,
            keyboardType = KeyboardType.Email,
            onValueChange = { email = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginInputField(
            text = "Passwort",
            value = password,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            onValueChange = { password = it }
        )

        Text(
            text = "Passwort vergessen?",
            color = Color.White,
            fontSize = 10.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(48.dp))

        PrimaryButton(
            text = "Einloggen",
            onClick = onLoginClick
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "oder weitermachen mit",
            color = Color.White,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .width(180.dp)
                .height(54.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFF4B5563),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "G  Google",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(42.dp))

        Row {
            Text(
                text = "Noch keinen Account? ",
                color = Color.White,
                fontSize = 12.sp
            )

            Text(
                text = "Registrieren",
                color = Color(0xFF22C55E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }
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
        placeholder = {
            Text(
                text = text,
                color = Color.Gray,
                fontSize = 13.sp
            )
        },
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