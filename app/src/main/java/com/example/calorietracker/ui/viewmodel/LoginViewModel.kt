package com.example.calorietracker.ui.viewmodel

import android.util.Log
import com.example.calorietracker.util.ExceptionMapper
import com.example.calorietracker.util.SessionManager
import io.github.jan.supabase.auth.exception.AuthRestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    override val error: String? = null,
    override val loading: Boolean = false,
    val isLoggedIn: Boolean = false,
) : UiState<LoginUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): LoginUiState {
        return this.copy(error = error, isLoggedIn = loading)
    }
}

class LoginViewModel(
    override val sessionManager: SessionManager,
    private val exceptionMapper: ExceptionMapper
) : BaseViewModel<LoginUiState>() {
    override val tag: String = "LoginViewModel"
    override val internalUiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = internalUiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: (String) -> Unit) {
        clearError()
        tryAndLogScope(
            onError = { e ->
                val mappedMessage = exceptionMapper.mapAuthError(e)
                internalUiState.update { it.copy(error = mappedMessage, loading = false) }
                Log.e(tag, "Failed to login", e)
            }
        ) {
            sessionManager.login(email = email, password = password)
            val userInfo = sessionManager.currentUserInfo
            if (userInfo == null) {
                val errorMsg =
                    "Login fehlgeschlagen: Benutzer konnte nicht authentifiziert werden."
                Log.e(tag, errorMsg)
                internalUiState.update { it.copy(loading = false, error = errorMsg) }
            } else {
                Log.d(tag, "Login erfolgreich für: ${userInfo.email}")
                internalUiState.update { it.copy(isLoggedIn = true, loading = false) }
                onSuccess(userInfo.id)
            }
        }
    }
}
