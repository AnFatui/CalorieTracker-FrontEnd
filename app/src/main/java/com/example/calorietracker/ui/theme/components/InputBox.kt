package com.example.calorietracker.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.reflect.typeOf

@Composable
inline fun <reified T> InputBox(
    label: String,
    value: T?,
    placeholder: String,
    modifier: Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    helperText: String? = null,
    crossinline validator: (T?) -> String? = { null },
    crossinline onValueChange: (T?) -> Unit
) {
    // Hilfsfunktion zur Formatierung: Verhindert ".0" bei ganzen Zahlen
    val formatValue = { v: T? ->
        when (v) {
            null -> ""
            is Double -> if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
            else -> v.toString()
        }
    }

    // Wir nutzen ein lokales State-Objekt für den Text, damit der Nutzer frei tippen kann (z.B. "70.")
    // ohne dass es sofort vom ViewModel-Format überschrieben wird.
    var textState by remember { mutableStateOf(formatValue(value)) }

    // Synchronisation bei externen Änderungen (z.B. Reset des Formulars)
    LaunchedEffect(value) {
        val isInt = typeOf<T>() == typeOf<Int?>() || typeOf<T>() == typeOf<Int>()
        val isDouble = typeOf<T>() == typeOf<Double?>() || typeOf<T>() == typeOf<Double>()

        val shouldUpdate = when {
            isInt -> textState.toIntOrNull() != (value as? Int)
            isDouble -> textState.replace(',', '.').toDoubleOrNull() != (value as? Double)
            else -> textState != (value as? String ?: "")
        }

        if (shouldUpdate) {
            textState = formatValue(value)
        }
    }

    val errorFromValidator = remember(value) { validator(value) }
    val finalIsError = isError || errorFromValidator != null
    val finalHelperText = errorFromValidator ?: helperText

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(
                    color = if (finalIsError) Color(0xFF450a0a) else Color(0xFF1F2937),
                    shape = RoundedCornerShape(16.dp)
                )
                // Hier steuern wir die inneren Abstände der gesamten Box
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp)
        ) {
            Text(
                label,
                color = if (finalIsError) Color(0xFFef4444) else Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            // Ein kleiner, definierter Abstand zwischen Label und Eingabe
            Spacer(modifier = Modifier.height(2.dp))

            val onInputChanged: (String) -> Unit = { input ->
                val isInt = typeOf<T>() == typeOf<Int?>() || typeOf<T>() == typeOf<Int>()
                val filtered = if (isInt) {
                    input.filter { it.isDigit() }
                } else {
                    input.replace(',', '.').filterIndexed { index, char ->
                        char.isDigit() || (char == '.' && input.indexOf('.') == index)
                    }
                }
                textState = filtered

                val newValue = if (isInt) {
                    filtered.toIntOrNull()
                } else {
                    filtered.toDoubleOrNull()
                }
                onValueChange(newValue as T?)
            }

            // Hilfsvariable für den Body-Typ-Check
            val isStringType = typeOf<T>() == typeOf<String?>() || typeOf<T>() == typeOf<String>()
            val isNumberType = typeOf<T>() == typeOf<Double?>() || typeOf<T>() == typeOf<Int?>() ||
                    typeOf<T>() == typeOf<Double>() || typeOf<T>() == typeOf<Int>()

            if (isStringType || isNumberType) {
                BasicTextField(
                    value = textState,
                    onValueChange = if (isStringType) {
                        { input -> textState = input; onValueChange(input as T?) }
                    } else onInputChanged,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp, // Bestimmt die feste Höhe
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        // Box stapelt den Placeholder flach unter/über den Text
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (textState.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = Color.Gray,
                                    fontSize = 18.sp, // MUSS identisch mit textStyle sein!
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Das eigentliche Eingabefeld liegt immer an derselben Stelle
                            innerTextField()
                        }
                    }
                )
            } else {
                throw IllegalArgumentException("Unsupported type '${typeOf<T>()}'")
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bleibt starr auf einer Zeilenhöhe, verschiebt nichts mehr
            Text(
                text = finalHelperText ?: " ",
                color = if (finalIsError) Color(0xFFef4444) else Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}