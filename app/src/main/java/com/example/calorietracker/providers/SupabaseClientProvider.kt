package com.example.calorietracker.providers

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://umvgaafgwytqqupsjija.supabase.co",
        supabaseKey = "sb_publishable_Nco4TRefKjZ-2J9JnvPq-A_PMEarzcl"
    ) {
        install(Postgrest)
        install(Auth) {
            sessionManager = SettingsSessionManager()
        }
        // Das eigentliche Storage-Modul für Bilder/Dateien
        install(Storage)
    }
}
