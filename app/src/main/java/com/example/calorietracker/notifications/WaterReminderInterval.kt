package com.example.calorietracker.notifications

enum class WaterReminderInterval(val minutes: Long, val label: String) {
    EVERY_30_MIN(30, "Alle 30 Minuten"),
    EVERY_50_MIN(50, "Alle 50 Minuten"),
    EVERY_HOUR(60, "Jede Stunde"),
    EVERY_90_MIN(90, "Alle 1,5 Stunden"),
    EVERY_2_HOURS(120, "Alle 2 Stunden");

    companion object {
        val DEFAULT = EVERY_HOUR

        fun fromMinutes(minutes: Long): WaterReminderInterval =
            entries.find { it.minutes == minutes } ?: DEFAULT
    }
}
