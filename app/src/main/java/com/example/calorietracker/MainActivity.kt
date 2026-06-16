package com.example.calorietracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorietracker.ui.theme.AppUiState
import com.example.calorietracker.ui.theme.CalorieTrackerTheme
import com.example.calorietracker.ui.theme.screens.*
import com.example.calorietracker.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CalorieTrackerTheme {
                // Zentrales UserViewModel für den globalen Profil-Status
                val userViewModel: UserViewModel = viewModel()
                val userState by userViewModel.uiState.collectAsState()
                
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                
                var currentScreen by remember { mutableStateOf(AppScreen.Login) }
                var appState by remember { mutableStateOf(AppUiState()) }
                var selectedMealTypeForAdd by remember { mutableStateOf<String?>(null) }

                // Automatischer Wechsel zum Login, wenn nicht eingeloggt
                LaunchedEffect(userState.isLoggedIn) {
                    if (!userState.isLoggedIn && currentScreen != AppScreen.Register) {
                        currentScreen = AppScreen.Login
                    }
                }

                val onShowMessage: (String) -> Unit = { message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }

                Scaffold(
                    containerColor = Color.Black,
                    snackbarHost = { 
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.padding(bottom = 64.dp) 
                        ) 
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                    ) {
                        when (currentScreen) {
                            AppScreen.Login -> LoginScreen(
                                onLoginSuccess = { 
                                    currentScreen = AppScreen.Home 
                                },
                                onRegisterClick = { currentScreen = AppScreen.Register },
                                onShowMessage = onShowMessage
                            )

                            AppScreen.Register -> RegisterScreen(
                                onCreateAccountSuccess = { currentScreen = AppScreen.Onboarding },
                                onBackToLoginClick = { currentScreen = AppScreen.Login },
                                onShowMessage = onShowMessage
                            )

                            AppScreen.Onboarding -> OnboardingScreen(
                                onContinueClick = { currentScreen = AppScreen.Home },
                                onShowMessage = onShowMessage
                            )

                            AppScreen.Home -> HomeScreen(
                                appState = appState.copy(
                                    name = userState.profile?.displayName ?: userState.profile?.username ?: ""
                                ),
                                onMealsClick = { currentScreen = AppScreen.Meals },
                                onAddMealClick = {
                                    selectedMealTypeForAdd = null
                                    currentScreen = AppScreen.AddMeal
                                },
                                onStatisticsClick = { currentScreen = AppScreen.Statistics },
                                onProfileClick = { currentScreen = AppScreen.Profile },
                                onWaterClick = { currentScreen = AppScreen.WaterTracking },
                                onWeightClick = { currentScreen = AppScreen.WeightTracking }
                            )

                            AppScreen.Profile -> ProfileScreen(
                                appState = appState.copy(
                                    name = userState.profile?.displayName ?: userState.profile?.username ?: ""
                                ),
                                onStateChange = { appState = it },
                                onHomeClick = { currentScreen = AppScreen.Home },
                                onMealsClick = { currentScreen = AppScreen.Meals },
                                onStatisticsClick = { currentScreen = AppScreen.Statistics },
                                onLogoutClick = {
                                    userViewModel.logout {
                                        appState = AppUiState()
                                        currentScreen = AppScreen.Login
                                    }
                                }
                            )

                            AppScreen.Meals -> MealsScreen(appState = appState, onHomeClick = {currentScreen = AppScreen.Home}, onStatisticsClick = {currentScreen = AppScreen.Statistics}, onProfileClick = {currentScreen = AppScreen.Profile}, onAddMealClick = {mealType -> selectedMealTypeForAdd = mealType; currentScreen = AppScreen.AddMeal}, onBottomAddClick = {selectedMealTypeForAdd = null; currentScreen = AppScreen.AddMeal}, onRecipeClick = {currentScreen = AppScreen.RecipeDetail})
                            AppScreen.Recipes -> RecipesScreen(onBackClick = {currentScreen = AppScreen.AddMeal}, onRecipeClick = {currentScreen = AppScreen.RecipeDetail})
                            AppScreen.RecipeDetail -> RecipeDetailScreen(onBackClick = {currentScreen = AppScreen.Recipes})
                            AppScreen.AddMeal -> AddMealScreen(appState = appState, selectedMealType = selectedMealTypeForAdd, onStateChange = {appState = it}, onBackClick = {currentScreen = AppScreen.Meals}, onRecipesClick = {currentScreen = AppScreen.Recipes})
                            AppScreen.Statistics -> StatisticsScreen(appState = appState, onHomeClick = {currentScreen = AppScreen.Home}, onMealsClick = {currentScreen = AppScreen.Meals}, onProfileClick = {currentScreen = AppScreen.Profile}, onWaterClick = {currentScreen = AppScreen.WaterTracking})
                            AppScreen.WaterTracking -> WaterTrackingScreen(appState = appState, onStateChange = {appState = it}, onBackClick = {currentScreen = AppScreen.Home})
                            AppScreen.WeightTracking -> WeightTrackingScreen(appState = appState, onStateChange = {appState = it}, onBackClick = {currentScreen = AppScreen.Home})
                        }
                    }
                }
            }
        }
    }
}

enum class AppScreen {
    Login, Register, Onboarding, Home, Meals, Recipes, RecipeDetail, AddMeal, Statistics, WaterTracking, WeightTracking, Profile
}
