package com.example.calorietracker.util

import com.example.calorietracker.data.model.ActivityLevel
import com.example.calorietracker.data.model.MetabolismType
import com.example.calorietracker.data.model.WeightStrategy

/**
 * Interface für verschiedene Kalorienberechnungsmodelle.
 */
interface CalorieCalculationStrategy {
    fun calculateGoals(
        metabolismType: MetabolismType,
        age: Int,
        heightCm: Int,
        weightKg: Double,
        activityLevel: ActivityLevel,
        weightStrategy: WeightStrategy,
        weeklyGoalKg: Double
    ): CalorieResult
}

data class CalorieResult(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

/**
 * Implementierung nach der Mifflin-St. Jeor Formel.
 */
class MifflinStJeorStrategy : CalorieCalculationStrategy {
    override fun calculateGoals(
        metabolismType: MetabolismType,
        age: Int,
        heightCm: Int,
        weightKg: Double,
        activityLevel: ActivityLevel,
        weightStrategy: WeightStrategy,
        weeklyGoalKg: Double
    ): CalorieResult {
        val bmr = when (metabolismType) {
            MetabolismType.FEMALE -> {
                (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161
            }

            MetabolismType.MALE -> {
                (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
            }
        }

        // Leistungsumsatz (TDEE) basierend auf Aktivitätslevel
        val pal = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 1.2
            ActivityLevel.LIGHTLY_ACTIVE -> 1.375
            ActivityLevel.MODERATELY_ACTIVE -> 1.55
            ActivityLevel.VERY_ACTIVE -> 1.725
            ActivityLevel.EXTREMELY_ACTIVE -> 1.9
        }

        val maintenanceCalories = (bmr * pal).toInt()

        // Anpassung basierend auf dem Ziel
        val calorieGoal = when (weightStrategy) {
            WeightStrategy.LOSE_WEIGHT -> maintenanceCalories - (weeklyGoalKg * 1000).toInt() // Grobe Faustformel: 1kg Fett approx 7000kcal, aber 1000kcal Defizit pro Tag für 1kg/Woche
            WeightStrategy.GAIN_WEIGHT -> maintenanceCalories + 300
            WeightStrategy.MAINTAIN_WEIGHT -> maintenanceCalories
        }

        // Makronährstoff-Verteilung (Beispiel: 30% Protein, 40% Carbs, 30% Fett)
        val protein = (calorieGoal * 0.30 / 4).toInt()
        val carbs = (calorieGoal * 0.40 / 4).toInt()
        val fat = (calorieGoal * 0.30 / 9).toInt()

        return CalorieResult(calorieGoal.coerceAtLeast(1200), protein, carbs, fat)
    }
}

object CalorieCalculator {
    private var strategy: CalorieCalculationStrategy = MifflinStJeorStrategy()

    fun setStrategy(newStrategy: CalorieCalculationStrategy) {
        strategy = newStrategy
    }

    fun calculate(
        metabolismType: MetabolismType,
        age: Int,
        heightCm: Int,
        weightKg: Double,
        activityLevel: ActivityLevel,
        weightStrategy: WeightStrategy,
        weeklyGoalKg: Double
    ): CalorieResult {
        return strategy.calculateGoals(
            metabolismType,
            age,
            heightCm,
            weightKg,
            activityLevel,
            weightStrategy,
            weeklyGoalKg
        )
    }
}
