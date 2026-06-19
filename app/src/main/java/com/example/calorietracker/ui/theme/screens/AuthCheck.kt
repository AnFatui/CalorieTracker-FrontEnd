package com.example.calorietracker.ui.theme.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.util.SessionManager

@Composable
fun AuthCheck(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onShowMessage: (String) -> Unit,
    sessionManager: SessionManager,
    profileRepository: ProfileRepository
) {
    LaunchedEffect(sessionManager.isLoggedIn) {
        if (!sessionManager.isLoggedIn) {
            onNavigateToLogin()
        } else {
            val userId = sessionManager.currentUserId
            if (userId == null) {
                Log.e("AuthCheck", "Failed to check for login, user id is null")
                onNavigateToLogin()
                return@LaunchedEffect
            }

            val profile = profileRepository.getProfile(userId)
            if (profile == null) {
                Log.e("AuthCheck", "Profile is null")
                return@LaunchedEffect
            }

            if (!profile.onboardingDone) onNavigateToOnboarding()
            else onNavigateToHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.Green)
    }
}
