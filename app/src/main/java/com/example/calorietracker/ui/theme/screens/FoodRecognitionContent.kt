package com.example.calorietracker.ui.theme.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.intl.Locale as ComposeLocale
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.FoodRecognitionUiState
import com.example.calorietracker.ui.viewmodel.FoodRecognitionViewModel
import com.example.calorietracker.util.ImageUploadUtils
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun FoodRecognitionContent(
    modifier: Modifier = Modifier,
    viewModel: FoodRecognitionViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var pendingCameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var preparingImage by remember {
        mutableStateOf(false)
    }

    val busy = preparingImage || uiState.loading

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                viewModel.clearResult()
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { photoWasTaken ->
            if (photoWasTaken) {
                selectedImageUri = pendingCameraUri
                viewModel.clearResult()
            }
        }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFF374151),
                    shape = RoundedCornerShape(24.dp)
                )
                .background(
                    color = Color(0xFF111827),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .background(
                            color = Color(0xFF1F2937),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📷",
                        fontSize = 34.sp
                    )
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Foto einer Mahlzeit erkennen",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = statusText(
                        selectedImageUri = selectedImageUri,
                        preparingImage = preparingImage,
                        uiState = uiState
                    ),
                    color = if (uiState.error != null) {
                        Color(0xFFF87171)
                    } else {
                        Color.Gray
                    },
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 26.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            text = "Foto auswählen",
            enabled = !busy,
            onClick = {
                galleryLauncher.launch("image/*")
            }
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(
                    color = Color(0xFF1F2937),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(enabled = !busy) {
                    val cameraUri =
                        createCameraImageUri(context)

                    pendingCameraUri = cameraUri
                    cameraLauncher.launch(cameraUri)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Kamera öffnen",
                color = if (busy) {
                    Color.White.copy(alpha = 0.45f)
                } else {
                    Color.White
                },
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        PrimaryButton(
            text = if (busy) {
                "Bild wird analysiert..."
            } else {
                "Bild analysieren"
            },
            enabled = selectedImageUri != null && !busy,
            onClick = {
                val imageUri = selectedImageUri

                if (imageUri == null) {
                    viewModel.showError(
                        "Wähle zuerst ein Bild aus."
                    )

                    return@PrimaryButton
                }

                coroutineScope.launch {
                    preparingImage = true
                    viewModel.clearResult()

                    try {
                        val imagePart =
                            ImageUploadUtils.prepareImage(
                                context = context,
                                imageUri = imageUri
                            )

                        viewModel.recognizeFood(imagePart)
                    } catch (exception: Exception) {
                        viewModel.showError(
                            exception.message
                                ?: "Das Bild konnte nicht vorbereitet werden."
                        )
                    } finally {
                        preparingImage = false
                    }
                }
            }
        )

        if (busy) {
            Spacer(Modifier.height(18.dp))

            CircularProgressIndicator(
                color = Color(0xFF22C55E)
            )
        }

        if (uiState.label != null) {
            Spacer(Modifier.height(20.dp))

            RecognitionResultCard(
                uiState = uiState
            )
        }
    }
}

private fun statusText(
    selectedImageUri: Uri?,
    preparingImage: Boolean,
    uiState: FoodRecognitionUiState
): String {
    if (preparingImage) {
        return "Das Bild wird vorbereitet."
    }

    if (uiState.loading) {
        return "Das Bild wird an den Server gesendet und analysiert."
    }

    if (uiState.error != null) {
        return uiState.error
    }

    if (uiState.label != null) {
        return "Erkennung abgeschlossen."
    }

    if (selectedImageUri != null) {
        return "Bild ausgewählt. Drücke jetzt auf „Bild analysieren“."
    }

    return "Wähle ein Bild aus oder nimm ein Foto auf."
}

@Composable
private fun RecognitionResultCard(
    uiState: FoodRecognitionUiState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF111827),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Text(
            text = "Erkanntes Gericht",
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = uiState.label ?: "Unbekannt",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        uiState.confidence?.let { confidence ->
            Spacer(Modifier.height(5.dp))

            Text(
                text = "Sicherheit: " +
                        String.format(ComposeLocale.current.platformLocale, "%.1f %%", confidence * 100.0),
                color = Color(0xFF22C55E),
                fontSize = 13.sp
            )
        }

        uiState.totalMilliseconds?.let { milliseconds ->
            Spacer(Modifier.height(5.dp))

            Text(
                text = "Gesamtdauer: " +
                        String.format(ComposeLocale.current.platformLocale, "%.0f ms", milliseconds),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "FatSecret Ergebnisse",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        if (uiState.foodResults.isEmpty()) {
            Text(
                text = "Keine passenden Lebensmittel gefunden.",
                color = Color.Gray,
                fontSize = 12.sp
            )
        } else {
            uiState.foodResults
                .forEachIndexed { index, food ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF1F2937),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "${index + 1}. ${food.name}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        if (food.description.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = food.description,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
        }
    }
}

private fun createCameraImageUri(
    context: Context
): Uri {
    val imageFile =
        File(
            context.cacheDir,
            "camera-${UUID.randomUUID()}.jpg"
        )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
