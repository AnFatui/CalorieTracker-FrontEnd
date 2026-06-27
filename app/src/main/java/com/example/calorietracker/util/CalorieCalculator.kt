package com.example.calorietracker.util

import com.example.calorietracker.data.model.Profile

/**
 * Interface für verschiedene Kalorienberechnungsmodelle.
 */
interface CalorieCalculationStrategy {
    fun calculateGoals(
        gender: String,
        age: Int,
        heightCm: Int,
        weightKg: Double,
        activityLevel: String,
        goal: String,
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
        gender: String,
        age: Int,
        heightCm: Int,
        weightKg: Double,
        activityLevel: String,
        goal: String,
        weeklyGoalKg: Double
    ): CalorieResult {
        // Grundumsatz (BMR)
        val bmr = if (gender.lowercase() == "weiblich") {
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161
        } else {
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
        }

        // Leistungsumsatz (TDEE) basierend auf Aktivitätslevel
        val pal = when (activityLevel) {
            "Sitzend" -> 1.2
            "Leicht aktiv" -> 1.375
            "Moderat aktiv" -> 1.55
            "Sehr aktiv" -> 1.725
            "Extrem aktiv" -> 1.9
            else -> 1.2
        }

        val maintenanceCalories = (bmr * pal).toInt()

        // Anpassung basierend auf dem Ziel
        val calorieGoal = when (goal) {
            "Abnehmen" -> maintenanceCalories - (weeklyGoalKg * 1000).toInt() // Grobe Faustformel: 1kg Fett approx 7000kcal, aber 1000kcal Defizit pro Tag für 1kg/Woche
            "Muskeln aufbauen" -> maintenanceCalories + 300
            else -> maintenanceCalories
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
        gender: String,
        age: Int,
        heightCm: Int,
        weightKg: Double,
        activityLevel: String,
        goal: String,
        weeklyGoalKg: Double
    ): CalorieResult {
        return strategy.calculateGoals(gender, age, heightCm, weightKg, activityLevel, goal, weeklyGoalKg)
    }
}
