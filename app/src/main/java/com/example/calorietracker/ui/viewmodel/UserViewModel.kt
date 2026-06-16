package com.example.calorietracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorietracker.data.model.Profile
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.providers.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserUiState(
    val profile: Profile? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

class UserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    private val repository = ProfileRepository()
    private val TAG = "UserViewModel"

    init {
        checkAuth()
    }

    private fun checkAuth() {
        val user = SupabaseClientProvider.supabase.auth.currentUserOrNull()
        if (user != null) {
            _uiState.update { it.copy(isLoggedIn = true) }
            loadProfile(user.id)
        }
    }

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val profile = repository.getProfile(userId)
            _uiState.update { it.copy(
                profile = profile, 
                isLoading = false, 
                isLoggedIn = true 
            ) }
            Log.d(TAG, "Profil geladen: $profile")
        }
    }

    fun updateProfile(profile: Profile, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.upsertProfile(profile)
                _uiState.update { it.copy(profile = profile, isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Updaten des Profils", e)
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.supabase.auth.signOut()
                _uiState.update { UserUiState() }
                onLogoutSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Logout", e)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
