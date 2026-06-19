package com.example.calorietracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.ui.theme.CalorieTrackerTheme
import com.example.calorietracker.ui.theme.screens.AddMealScreen
import com.example.calorietracker.ui.theme.screens.AuthCheck
import com.example.calorietracker.ui.theme.screens.HomeScreen
import com.example.calorietracker.ui.theme.screens.LoginScreen
import com.example.calorietracker.ui.theme.screens.MealsScreen
import com.example.calorietracker.ui.theme.screens.OnboardingScreen
import com.example.calorietracker.ui.theme.screens.ProfileScreen
import com.example.calorietracker.ui.theme.screens.RecipesScreen
import com.example.calorietracker.ui.theme.screens.RegisterScreen
import com.example.calorietracker.ui.theme.screens.StatisticsScreen
import com.example.calorietracker.ui.theme.screens.WaterTrackingScreen
import com.example.calorietracker.ui.theme.screens.WeightTrackingScreen
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CalorieTrackerTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                val onShowMessage: (String) -> Unit = { message ->
                    scope.launch { snackbarHostState.showSnackbar(message) }
                }

                Scaffold(
                    containerColor = Color.Black, snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.padding(bottom = 64.dp)
                        )
                    }) { paddingValues ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues),
                        onShowMessage = onShowMessage
                    )
                }
            }
        }
    }

    @Composable
    fun AppNavHost(
        navController: NavHostController, onShowMessage: (String) -> Unit, modifier: Modifier
    ) {
        NavHost(
            navController = navController, startDestination = AuthCheck, modifier = modifier
        ) {
            composable<AuthCheck> {
                val sessionManager = get<SessionManager>()
                val profileRepository = get<ProfileRepository>()

                AuthCheck(
                    sessionManager = sessionManager,
                    profileRepository = profileRepository,
                    onShowMessage = onShowMessage,
                    onNavigateToLogin = {
                        navController.navigate(Login) {
                            popUpTo(AuthCheck) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Home) {
                            popUpTo(AuthCheck) { inclusive = true }
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Onboarding) {
                            popUpTo(AuthCheck) { inclusive = true }
                        }
                    })
            }

            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Home) {
                            popUpTo(Login) {
                                inclusive = true
                            }
                        }
                    },
                    onRegisterClick = { navController.navigate(Register) },
                    onShowMessage = onShowMessage
                )
            }

            composable<Register> {
                RegisterScreen(
                    viewModel = koinViewModel(),
                    onCreateAccountSuccess = { navController.navigate(Onboarding) },
                    onBackToLoginClick = { navController.navigate(Login) },
                    onShowMessage = onShowMessage,
                )
            }

            composable<Home> {
                HomeScreen(
                    viewModel = koinViewModel(),
                    onProfileClick = { navController.navigate(ProfileDetails) },
                    onMealsClick = { navController.navigate(MealTracking) },
                    onAddMealClick = { navController.navigate(AddMeal) },
                    onStatisticsClick = { navController.navigate(Statistics) },
                    onWaterClick = { navController.navigate(WaterTracking) },
                    onWeightClick = { navController.navigate(WeightTracking) }
                )
            }

            composable<WaterTracking> {
                WaterTrackingScreen(
                    viewModel = koinViewModel(),
                    onBackClick = { navController.navigate(Home) },
                    onShowMessage = onShowMessage,
                )
            }

            composable<Onboarding> {
                OnboardingScreen(
                    viewModel = koinViewModel(),
                    onContinueClick = {},
                    onShowMessage = onShowMessage,
                )
            }

            composable<Statistics> {
                StatisticsScreen(
                    viewModel = koinViewModel(),
                    onWaterClick = { navController.navigate(WaterTracking) },
                    onHomeClick = { navController.navigate(Home) },
                    onMealsClick = { navController.navigate(MealTracking) },
                    onProfileClick = { navController.navigate(ProfileDetails) },
                )
            }

            composable<WeightTracking> {
                WeightTrackingScreen(
                    viewModel = koinViewModel(),
                    onBackClick = { navController.navigate(Home) },
                )
            }

            composable<Recipes> {
                RecipesScreen(
                    viewModel = koinViewModel(),
                    onBackClick = { navController.navigate(Home) },
                    onRecipeClick = { }
                )
            }

            composable<ProfileDetails> {
                ProfileScreen(
                    viewModel = koinViewModel(),
                    onMealsClick = { navController.navigate(MealTracking) },
                    onHomeClick = { navController.navigate(Home) },
                    onLogoutClick = { },
                    onStatisticsClick = { }
                )
            }

            composable<MealTracking> {
                MealsScreen(
                    viewModel = koinViewModel(),
                    onStatisticsClick = { navController.navigate(Statistics) },
                    onHomeClick = { navController.navigate(Home) },
                    onRecipeClick = { navController.navigate(Recipes) },
                    onProfileClick = { navController.navigate(ProfileDetails) },
                    onAddMealClick = { navController.navigate(AddMeal) },
                    onBottomAddClick = { }
                )
            }

            composable<AddMeal> {
                AddMealScreen(
                    viewModel = koinViewModel(),
                    onBackClick = { navController.navigate(Home) },
                    onRecipesClick = { navController.navigate(Recipes) }
                )
            }
        }
    }
}
