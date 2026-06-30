package com.example.calorietracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorietracker.network.BackendClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.json.JSONArray
import org.json.JSONObject

data class RecognizedFoodItem(
    val name: String,
    val description: String
)

data class FoodRecognitionUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val label: String? = null,
    val confidence: Double? = null,
    val totalMilliseconds: Double? = null,
    val foodResults: List<RecognizedFoodItem> = emptyList()
)

class FoodRecognitionViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(FoodRecognitionUiState())

    val uiState =
        _uiState.asStateFlow()

    fun recognizeFood(
        image: MultipartBody.Part
    ) {
        viewModelScope.launch {
            _uiState.value =
                FoodRecognitionUiState(
                    loading = true
                )

            try {
                val response =
                    BackendClient.api.recognizeFood(
                        userId = "android-app",
                        image = image
                    )

                if (!response.isSuccessful) {
                    val errorText =
                        response.errorBody()?.string()

                    _uiState.value =
                        FoodRecognitionUiState(
                            error =
                                "Serverfehler ${response.code()}: " +
                                        (
                                                errorText
                                                    ?: "Keine Fehlerdetails"
                                                )
                        )

                    return@launch
                }

                val responseText =
                    response.body()?.string()

                if (responseText == null) {
                    _uiState.value =
                        FoodRecognitionUiState(
                            error =
                                "Der Server hat keine Antwort gesendet."
                        )

                    return@launch
                }

                _uiState.value =
                    parseResponse(responseText)

            } catch (exception: Exception) {
                _uiState.value =
                    FoodRecognitionUiState(
                        error =
                            exception.message
                                ?: "Die Bilderkennung ist fehlgeschlagen."
                    )
            }
        }
    }

    fun clearResult() {
        _uiState.value =
            FoodRecognitionUiState()
    }

    fun showError(
        message: String
    ) {
        _uiState.value =
            FoodRecognitionUiState(
                error = message
            )
    }

    private fun parseResponse(
        responseText: String
    ): FoodRecognitionUiState {
        val root =
            JSONObject(responseText)

        val classifier =
            root.optJSONObject("classifier")
                ?: throw IllegalStateException(
                    "Die Serverantwort enthält kein classifier-Ergebnis."
                )

        val label =
            classifier
                .optString("label")
                .ifBlank {
                    "Unbekannt"
                }

        val confidence =
            classifier.optDouble(
                "confidence",
                0.0
            )

        val timing =
            root.optJSONObject("timing")

        val totalMilliseconds =
            if (
                timing != null &&
                timing.has("totalMs") &&
                !timing.isNull("totalMs")
            ) {
                timing.optDouble("totalMs")
            } else {
                null
            }

        return FoodRecognitionUiState(
            label = label,
            confidence = confidence,
            totalMilliseconds =
                totalMilliseconds,
            foodResults =
                readFoodResults(root)
        )
    }

    private fun readFoodResults(
        root: JSONObject
    ): List<RecognizedFoodItem> {
        val fatSecret =
            root.optJSONObject("fatSecret")
                ?: return emptyList()

        val foods =
            fatSecret.optJSONObject("foods")
                ?: return emptyList()

        val foodValue =
            foods.opt("food")
                ?: return emptyList()

        val foodObjects =
            mutableListOf<JSONObject>()

        when (foodValue) {
            is JSONArray -> {
                for (
                index in
                0 until foodValue.length()
                ) {
                    val foodObject =
                        foodValue.optJSONObject(index)

                    if (foodObject != null) {
                        foodObjects.add(foodObject)
                    }
                }
            }

            is JSONObject -> {
                foodObjects.add(foodValue)
            }
        }

        return foodObjects
            .take(10)
            .map { foodObject ->
                RecognizedFoodItem(
                    name =
                        foodObject.optString(
                            "food_name",
                            "Unbekannt"
                        ),
                    description =
                        foodObject.optString(
                            "food_description",
                            ""
                        )
                )
            }
    }
}