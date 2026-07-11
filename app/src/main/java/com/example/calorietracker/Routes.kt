package com.example.calorietracker

import com.example.calorietracker.ui.theme.screens.ProfilePage
import kotlinx.serialization.Serializable

@Serializable
object AuthCheck

@Serializable
object Home

@Serializable
object Login

@Serializable
object Register

@Serializable
object WaterTracking

@Serializable
object Onboarding

@Serializable
object Statistics

@Serializable
object WeightTracking

@Serializable
object MealTracking

@Serializable
object Recipes

@Serializable
data class ProfileDetails(val page: ProfilePage = ProfilePage.OVERVIEW)

@Serializable
object AddMeal

@Serializable
object Fasting

@Serializable
object StepTracking
