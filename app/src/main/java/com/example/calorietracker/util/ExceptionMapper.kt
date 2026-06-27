package com.example.calorietracker.util

import io.github.jan.supabase.auth.exception.AuthRestException

class ExceptionMapper {
    fun mapAuthError(e: Exception): String {
        return when (e) {
            is AuthRestException -> {
                when (e.error) {
                    "user_already_exists" -> "Diese E-Mail-Adresse wird bereits verwendet."
                    "invalid_email" -> "Die E-Mail-Adresse ist ungültig."
                    "weak_password" -> "Das Passwort ist zu schwach (mind. 6 Zeichen)."
                    "signup_disabled" -> "Die Registrierung ist aktuell deaktiviert."
                    "validation_failed" -> ""
                    else -> "Registrierungsfehler: ${e.error}"
                }
            }

            else -> e.localizedMessage ?: "Ein unerwarteter Fehler ist aufgetreten"
        }
    }
}
