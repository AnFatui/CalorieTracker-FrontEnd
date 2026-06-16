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

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val TAG = "LoginViewModel"

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                SupabaseClientProvider.supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                val user = SupabaseClientProvider.supabase.auth.currentUserOrNull()
                if (user != null) {
                    Log.d(TAG, "Login erfolgreich für: ${user.email}")
                    _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
                    onSuccess()
                } else {
                    val errorMsg = "Login fehlgeschlagen: Benutzer konnte nicht authentifiziert werden."
                    Log.e(TAG, errorMsg)
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
            } catch (e: Exception) {
                val errorMsg = mapAuthError(e)
                Log.e(TAG, "Login Fehler: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    private fun mapAuthError(e: Exception): String {
        return when (e) {
            is AuthRestException -> {
                when (e.error) {
                    "invalid_credentials" -> "E-Mail oder Passwort ist falsch."
                    "user_not_found" -> "Benutzer wurde nicht gefunden."
                    "invalid_grant" -> "Ungültige Anmeldedaten."
                    else -> "Authentifizierungsfehler: ${e.error}"
                }
            }
            else -> e.localizedMessage ?: "Ein unerwarteter Fehler ist aufgetreten"
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
