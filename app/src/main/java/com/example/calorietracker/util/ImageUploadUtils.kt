package com.example.calorietracker.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object ImageUploadUtils {

    private const val MAX_IMAGE_SIZE_BYTES =
        10 * 1024 * 1024

    suspend fun prepareImage(
        context: Context,
        imageUri: Uri
    ): MultipartBody.Part {
        return withContext(Dispatchers.IO) {
            val resolver =
                context.contentResolver

            val contentType =
                resolver.getType(imageUri)
                    ?: "image/jpeg"

            if (!contentType.startsWith("image/")) {
                throw IllegalArgumentException(
                    "Die ausgewählte Datei ist kein Bild."
                )
            }

            val imageBytes =
                resolver.openInputStream(imageUri)?.use { input ->
                    input.readBytes()
                } ?: throw IllegalArgumentException(
                    "Das Bild konnte nicht geöffnet werden."
                )

            if (imageBytes.isEmpty()) {
                throw IllegalArgumentException(
                    "Das Bild ist leer."
                )
            }

            if (imageBytes.size > MAX_IMAGE_SIZE_BYTES) {
                throw IllegalArgumentException(
                    "Das Bild ist größer als 10 MB."
                )
            }

            val requestBody =
                imageBytes.toRequestBody(
                    contentType.toMediaType()
                )

            MultipartBody.Part.createFormData(
                name = "image",
                filename = "meal-image.jpg",
                body = requestBody
            )
        }
    }
}