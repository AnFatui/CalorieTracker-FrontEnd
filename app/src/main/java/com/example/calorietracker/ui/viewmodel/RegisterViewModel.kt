package com.example.calorietracker.ui.viewmodel

import android.util.Log
import com.example.calorietracker.util.ExceptionMapper
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class RegisterUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val isRegistered: Boolean = false
) : UiState<RegisterUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): RegisterUiState {
        return this.copy(loading = loading, error = error)
    }
}

class RegisterViewModel(
    override val sessionManager: SessionManager,
    private val exceptionMapper: ExceptionMapper
) : BaseViewModel<RegisterUiState>() {
    override val internalUiState = MutableStateFlow(RegisterUiState())
    override val tag: String = "RegisterViewModel"

    val uiState: StateFlow<RegisterUiState> = internalUiState.asStateFlow()

    fun signUp(name: String, email: String, password: String, onSuccess: (String) -> Unit) {
        tryAndLogScope(
            onError = { e ->
                val message = exceptionMapper.mapAuthError(e)
                internalUiState.update { it.copy(error = message) }
                Log.e(tag, "Failed to register user", e)
            }
        ) {
            sessionManager.signUp(
                email = email,
                password = password,
                data = buildJsonObject { put("display_name", name) }
            );

            getUserId { userId ->
                onSuccess(userId)
            }
        }
    }
}
