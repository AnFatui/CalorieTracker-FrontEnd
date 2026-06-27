package com.example.calorietracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : UiState<S>> : ViewModel() {
    protected abstract val internalUiState: MutableStateFlow<S>
    protected abstract val tag: String
    protected abstract val sessionManager: SessionManager

    protected fun tryAndLogScope(
        onError: ((Exception) -> Unit)? = null,
        action: suspend () -> Unit
    ) {
        internalUiState.update { it.copyFlags(loading = true) }
        viewModelScope.launch {
            try {
                action()
            } catch (e: Exception) {
                if (onError != null) {
                    onError(e)
                } else {
                    Log.e(tag, "Operation failed", e)
                    internalUiState.update {
                        it.copyFlags(
                            loading = false,
                            error = "Operation failed" // TODO: localization
                        )
                    }
                }
            } finally {
                internalUiState.update { it.copyFlags(loading = false) }
            }
        }
    }

    suspend fun getUserId(action: suspend (String) -> Unit) {
        val userId = sessionManager.currentUserId
        if (userId == null) {
            Log.e(tag, "User id is null")
            internalUiState.update { it.copyFlags(error = "User session invalid") } // TODO: localization
            return
        }

        action(userId)
    }

    fun clearError() {
        internalUiState.update { it.copyFlags(error = null) }
    }
}
