package com.example.calorietracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.ui.theme.CalorieTrackerTheme
import com.example.calorietracker.ui.theme.components.AppBottomBar
import com.example.calorietracker.ui.theme.icons.MaterialSymbolsArrowBackIosNew
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
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            CalorieTrackerTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                var title by remember { mutableStateOf("Home") }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Back Button
                val canPop = navController.previousBackStackEntry != null
                val isRootScreen = currentDestination?.hasRoute<Home>() == true ||
                        currentDestination?.hasRoute<Login>() == true ||
                        currentDestination?.hasRoute<AuthCheck>() == true

                val onShowMessage: (String) -> Unit = { message ->
                    scope.launch { snackbarHostState.showSnackbar(message) }
                }

                val onSetTitle: (String) -> Unit = {
                    title = it;
                }

                Scaffold(
                    containerColor = Color.Black,
                    topBar = {
                        val canNavigateBack = canPop && !isRootScreen
                        AppTopBar(
                            title = title,
                            showBackButton = canNavigateBack,
                            onNavigateBack = { navController.popBackStack() })
                    },
                    bottomBar = {
                        AppBottomBar(
                            onAddClick = { navController.navigate(AddMeal) },
                            onHomeClick = { navController.navigate(Home) },
                            onMealsClick = { navController.navigate(MealTracking) },
                            onProfileClick = { navController.navigate(ProfileDetails) },
                            onStatisticsClick = { navController.navigate(Statistics) },
                            currentDestination = currentDestination
                        )
                    },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.padding(bottom = 64.dp)
                        )
                    }) { paddingValues ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues),
                        onShowMessage = onShowMessage,
                        onSetTitle = onSetTitle
                    )
                }
            }
        }
    }

    @Composable
    fun AppNavHost(
        navController: NavHostController,
        onShowMessage: (String) -> Unit,
        onSetTitle: (String) -> Unit,
        modifier: Modifier
    ) {
        NavHost(
            navController = navController,
            startDestination = AuthCheck,
            modifier = modifier
        ) {
            composable<AuthCheck> {
                val sessionManager = get<SessionManager>()
                val profileRepository = get<ProfileRepository>()

                onSetTitle("")

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
                    }
                )
            }

            composable<Login> {
                onSetTitle("Login")

                LoginScreen(
                    viewModel = koinViewModel(),
                    onLoginSuccess = {
                        navController.navigate(Home) {
                            popUpTo(Login) {
                                inclusive = true
                            }
                        }
                    },
                    onRegisterClick = { navController.navigate(Register) },
                    onShowMessage = onShowMessage,
                )
            }

            composable<Register> {
                onSetTitle("Register")

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
                    onWaterClick = { navController.navigate(WaterTracking) },
                    onWeightClick = { navController.navigate(WeightTracking) },
                    onSetTitle = { onSetTitle(it) }
                )
            }

            composable<WaterTracking> {
                onSetTitle("")

                WaterTrackingScreen(
                    viewModel = koinViewModel(),
                    onBackClick = { navController.navigate(Home) },
                    onShowMessage = onShowMessage,
                )
            }

            composable<Onboarding> {
                onSetTitle("")

                OnboardingScreen(
                    viewModel = koinViewModel(),
                    onContinueClick = {},
                    onShowMessage = onShowMessage,
                )
            }

            composable<Statistics> {
                onSetTitle("")

                StatisticsScreen(
                    viewModel = koinViewModel(),
                    onWaterClick = { navController.navigate(WaterTracking) },
                )
            }

            composable<WeightTracking> {
                onSetTitle("Gewicht")

                WeightTrackingScreen(
                    viewModel = koinViewModel(),
                )
            }

            composable<Recipes> {
                onSetTitle("")

                RecipesScreen(
                    viewModel = koinViewModel(),
                    onBackClick = { navController.navigate(Home) },
                    onRecipeClick = { }
                )
            }

            composable<ProfileDetails> {
                onSetTitle("")

                ProfileScreen(
                    viewModel = koinViewModel(),
                    onLogoutClick = { navController.navigate(Login) },
                )
            }

            composable<MealTracking> {
                onSetTitle("")

                MealsScreen(
                    viewModel = koinViewModel(),
                    onRecipeClick = { navController.navigate(Recipes) },
                    onAddMealClick = { navController.navigate(AddMeal) },
                )
            }

            composable<AddMeal> {
                onSetTitle("")

                AddMealScreen(
                    viewModel = koinViewModel(),
                    onBackClick = { navController.navigate(Home) },
                    onRecipesClick = { navController.navigate(Recipes) }
                )
            }
        }
    }

    @Composable
    fun AppTopBar(title: String, showBackButton: Boolean, onNavigateBack: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .statusBarsPadding()
                .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(
                        imageVector = MaterialSymbolsArrowBackIosNew,
                        contentDescription = "Zurück",
                        tint = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
