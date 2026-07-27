package com.example.calorietracker.ui.viewmodel

import android.util.Log
import com.example.calorietracker.util.ExceptionMapper
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ResetPasswordUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val isPasswordUpdated: Boolean = false
) : UiState<ResetPasswordUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): ResetPasswordUiState {
        return this.copy(loading = loading, error = error)
    }
}

class ResetPasswordViewModel(
    override val sessionManager: SessionManager,
    private val exceptionMapper: ExceptionMapper
) : BaseViewModel<ResetPasswordUiState>() {
    override val internalUiState = MutableStateFlow(ResetPasswordUiState())
    override val tag: String = "ResetPasswordViewModel"

    val uiState: StateFlow<ResetPasswordUiState> = internalUiState.asStateFlow()

    fun updatePassword(newPassword: String, onSuccess: () -> Unit) {
        tryAndLogScope(
            onError = { e ->
                val message = exceptionMapper.mapAuthError(e)
                internalUiState.update { it.copy(error = message, loading = false) }
                Log.e(tag, "Failed to update password", e)
            }
        ) {
            sessionManager.updatePassword(newPassword)
            internalUiState.update { it.copy(isPasswordUpdated = true, loading = false) }
            onSuccess()
        }
    }
}
