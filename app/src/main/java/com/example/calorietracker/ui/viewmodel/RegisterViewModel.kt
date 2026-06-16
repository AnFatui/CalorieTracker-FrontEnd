package com.example.calorietracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorietracker.providers.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistered: Boolean = false
)

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val TAG = "RegisterViewModel"

    fun signUp(name: String, email: String, password: String, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                SupabaseClientProvider.supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    this.data = buildJsonObject {
                        put("display_name", name)
                    }
                }
                
                Log.d(TAG, "Registrierung erfolgreich für: $email")
                _uiState.update { it.copy(isRegistered = true, isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = mapAuthError(e)
                Log.e(TAG, "Register Fehler: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    private fun mapAuthError(e: Exception): String {
        return when (e) {
            is AuthRestException -> {
                when (e.error) {
                    "user_already_exists" -> "Diese E-Mail-Adresse wird bereits verwendet."
                    "invalid_email" -> "Die E-Mail-Adresse ist ungültig."
                    "weak_password" -> "Das Passwort ist zu schwach (mind. 6 Zeichen)."
                    "signup_disabled" -> "Die Registrierung ist aktuell deaktiviert."
                    "validation_failed" -> ""
                    else -> "Registrierungsfehler: ${e.error}"
                }
            }
            else -> e.localizedMessage ?: "Ein unerwarteter Fehler ist aufgetreten"
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
