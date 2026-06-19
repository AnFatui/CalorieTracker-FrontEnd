package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val calorieGoal: Int? = null,
    val weightStrategy: String? = null,
    val avatarUrl: String? = null,
    val displayName: String
) : UiState<ProfileUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): ProfileUiState {
        return this.copyFlags(error = error, loading = loading)
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
        asdf
    }

    fun updateProfileImage(imageBytes: ByteArray) {
        tryAndLogScope {
            getUserId { userId ->
                profileRepository.uploadAvatar(
                    userId,
                    imageBytes
                )
            }
        }
    }
}
