package com.example.calorietracker

import com.example.calorietracker.data.local.NotificationPreferences
import com.example.calorietracker.data.repository.FastingScheduleRepository
import com.example.calorietracker.data.repository.HealthConnectRepository
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.data.repository.WaterRepository
import com.example.calorietracker.data.repository.WeightRepository
import com.example.calorietracker.notifications.NotificationScheduler
import com.example.calorietracker.providers.SupabaseClientProvider
import com.example.calorietracker.ui.viewmodel.AddMealViewModel
import com.example.calorietracker.ui.viewmodel.FastingViewModel
import com.example.calorietracker.ui.viewmodel.HomeViewModel
import com.example.calorietracker.ui.viewmodel.LoginViewModel
import com.example.calorietracker.ui.viewmodel.MealTrackingViewModel
import com.example.calorietracker.ui.viewmodel.NotificationSettingsViewModel
import com.example.calorietracker.ui.viewmodel.OnboardingViewModel
import com.example.calorietracker.ui.viewmodel.ProfileViewModel
import com.example.calorietracker.ui.viewmodel.RecipeDetailViewModel
import com.example.calorietracker.ui.viewmodel.RecipesViewModel
import com.example.calorietracker.ui.viewmodel.RegisterViewModel
import com.example.calorietracker.ui.viewmodel.StatisticsViewModel
import com.example.calorietracker.ui.viewmodel.StepTrackingViewModel
import com.example.calorietracker.ui.viewmodel.WaterTrackingViewModel
import com.example.calorietracker.ui.viewmodel.WeightTrackingViewModel
import com.example.calorietracker.util.ExceptionMapper
import com.example.calorietracker.util.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Repos
    single { FastingScheduleRepository(get()) }
    single { ProfileRepository(get()) }
    single { WaterRepository(get()) }
    single { WeightRepository(get()) }
    single { HealthConnectRepository(androidContext()) }

    // Services / utils
    single(createdAtStart = true) { SessionManager(get()) }
    single { SupabaseClientProvider.supabase }
    single { ExceptionMapper() }
    single { NotificationPreferences(androidContext()) }
    single { NotificationScheduler(androidContext()) }

    // ViewModels
    viewModelOf(::AddMealViewModel)
    viewModelOf(::FastingViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::MealTrackingViewModel)
    viewModelOf(::NotificationSettingsViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::RecipesViewModel)
    viewModelOf(::RecipeDetailViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::StatisticsViewModel)
    viewModelOf(::StepTrackingViewModel)
    viewModelOf(::WaterTrackingViewModel)
    viewModelOf(::WeightTrackingViewModel)
}
