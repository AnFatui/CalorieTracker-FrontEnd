package com.example.calorietracker.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val calorieGoal: Int? = null,
    val weightStrategy: String? = null,
    val avatarUrl: String? = null,
    val displayName: String? = null
) : UiState<ProfileUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): ProfileUiState {
        return this.copy(error = error, loading = loading)
    }
}

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    override val sessionManager: SessionManager,
) : BaseViewModel<ProfileUiState>() {
    override val internalUiState = MutableStateFlow(ProfileUiState())
    override val tag: String = "ProfileViewModel"

    val uiState: StateFlow<ProfileUiState> = internalUiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        tryAndLogScope {
            val currentUserId = sessionManager.currentUserId;
            if (currentUserId == null) {
                internalUiState.update { it.copy(error = "Failed, no user id found") }
                return@tryAndLogScope
            }

            val profile = profileRepository.getProfile(currentUserId)
            if (profile == null) {
                internalUiState.update { it.copy(error = "Failed to load profile") }
                return@tryAndLogScope
            }

            internalUiState.update {
                it.copy(
                    displayName = profile.displayName,
                    calorieGoal = profile.calorieGoal,
                    avatarUrl = profile.avatarUrl
                )
            }
        }
    }

    fun logout() {
        tryAndLogScope {
            sessionManager.logout()
        }
    }

    fun updateProfileImage(imageBytes: ByteArray) {
        tryAndLogScope {
            getUserId { userId ->
                val newUrl = profileRepository.uploadAvatar(
                    userId,
                    imageBytes
                )

                val cacheBusterUrl = "$newUrl?t=${System.currentTimeMillis()}"
                internalUiState.update { it.copy(avatarUrl = cacheBusterUrl) }
            }
        }
    }
}
