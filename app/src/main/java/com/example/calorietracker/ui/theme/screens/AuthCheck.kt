package com.example.calorietracker.ui.theme.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = "file:///android_asset/macromate_icon_neu.png",
                contentDescription = "MacroMate Logo",
                modifier = Modifier.size(96.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "CalorieTracker",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
