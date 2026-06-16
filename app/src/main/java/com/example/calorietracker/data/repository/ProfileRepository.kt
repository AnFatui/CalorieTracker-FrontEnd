package com.example.calorietracker.data.repository

import com.example.calorietracker.data.model.Profile
import com.example.calorietracker.providers.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository {

    suspend fun getProfile(userId: String): Profile? = withContext(Dispatchers.IO) {
        try {
            SupabaseClientProvider.supabase.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun upsertProfile(profile: Profile) = withContext(Dispatchers.IO) {
        SupabaseClientProvider.supabase.from("profiles").upsert(profile)
    }
}
