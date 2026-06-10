package com.example.calorietracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.calorietracker.ui.theme.CalorieTrackerTheme
import com.example.calorietracker.ui.theme.screens.*
import com.example.calorietracker.ui.theme.AppUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CalorieTrackerTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.Login) }
                var appState by remember { mutableStateOf(AppUiState()) }

                when (currentScreen) {
                    AppScreen.Login -> LoginScreen(
                        onLoginClick = { currentScreen = AppScreen.Onboarding },
                        onRegisterClick = { currentScreen = AppScreen.Register }
                    )

                    AppScreen.Register -> RegisterScreen(
                        onCreateAccountClick = { currentScreen = AppScreen.Onboarding },
                        onBackToLoginClick = { currentScreen = AppScreen.Login }
                    )

                    AppScreen.Onboarding -> OnboardingScreen(
                        appState = appState,
                        onStateChange = { appState = it },
                        onContinueClick = { currentScreen = AppScreen.Home }
                    )

                    AppScreen.Home -> HomeScreen(
                        appState = appState,
                        onMealsClick = { currentScreen = AppScreen.Meals },
                        onStatisticsClick = { currentScreen = AppScreen.Statistics },
                        onProfileClick = { currentScreen = AppScreen.Profile },
                        onWaterClick = { currentScreen = AppScreen.WaterTracking },
                        onWeightClick = { currentScreen = AppScreen.WeightTracking }
                    )

                    AppScreen.Meals -> MealsScreen(
                        onHomeClick = { currentScreen = AppScreen.Home },
                        onStatisticsClick = { currentScreen = AppScreen.Statistics },
                        onProfileClick = { currentScreen = AppScreen.Profile },
                        onAddMealClick = { currentScreen = AppScreen.AddMeal },
                        onRecipeClick = { currentScreen = AppScreen.RecipeDetail }
                    )

                    AppScreen.Recipes -> RecipesScreen(
                        onRecipeClick = { currentScreen = AppScreen.RecipeDetail }
                    )

                    AppScreen.RecipeDetail -> RecipeDetailScreen(
                        onBackClick = { currentScreen = AppScreen.Recipes }
                    )

                    AppScreen.AddMeal -> AddMealScreen(
                        appState = appState,
                        onStateChange = { appState = it },
                        onBackClick = { currentScreen = AppScreen.Meals }
                    )

                    AppScreen.Statistics -> StatisticsScreen(
                        appState = appState,
                        onHomeClick = { currentScreen = AppScreen.Home },
                        onMealsClick = { currentScreen = AppScreen.Meals },
                        onProfileClick = { currentScreen = AppScreen.Profile },
                        onWaterClick = { currentScreen = AppScreen.WaterTracking }
                    )

                    AppScreen.WaterTracking -> WaterTrackingScreen(
                        appState = appState,
                        onStateChange = { appState = it },
                        onBackClick = { currentScreen = AppScreen.Home }
                    )

                    AppScreen.WeightTracking -> WeightTrackingScreen(
                        appState = appState,
                        onStateChange = { appState = it },
                        onBackClick = { currentScreen = AppScreen.Home }
                    )

                    AppScreen.Profile -> ProfileScreen(
                        onHomeClick = { currentScreen = AppScreen.Home },
                        onMealsClick = { currentScreen = AppScreen.Meals },
                        onStatisticsClick = { currentScreen = AppScreen.Statistics },
                        onLogoutClick = { currentScreen = AppScreen.Login }
                    )
                }
            }
        }
    }
}

enum class AppScreen {
    Login,
    Register,
    Onboarding,
    Home,
    Meals,
    Recipes,
    RecipeDetail,
    AddMeal,
    Statistics,
    WaterTracking,
    WeightTracking,
    Profile
}