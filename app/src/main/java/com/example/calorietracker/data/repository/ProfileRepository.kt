package com.example.calorietracker.data.repository

import com.example.calorietracker.data.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class ProfileRepository(
    private val supabase: SupabaseClient
) {
    suspend fun getProfile(userId: String): Profile? = withContext(Dispatchers.IO) {
        // Load profile from table
        val profile = supabase.from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<Profile>()

        // Load additional data from auth table
        // Use retrieveUserForCurrentSession to get the latest data from the server (e.g. after email confirmation)
        val user = try {
            supabase.auth.retrieveUserForCurrentSession(updateSession = true)
        } catch (_: Exception) {
            supabase.auth.currentUserOrNull()
        }

        val displayName = user?.userMetadata?.get("display_name")?.jsonPrimitive?.content
        val email = user?.email

        // Assign the data
        profile?.copy(displayName = displayName, email = email)
    }

    suspend fun upsertProfile(profile: Profile) = withContext(Dispatchers.IO) {
        supabase.from("profiles").upsert(profile)
        
        profile.displayName?.let { name ->
            supabase.auth.updateUser {
                data = buildJsonObject {
                    put("display_name", name)
                }
            }
        }
    }

    suspend fun updateEmail(newEmail: String) = withContext(Dispatchers.IO) {
        supabase.auth.updateUser {
            email = newEmail
        }
    }

    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): String =
        withContext(Dispatchers.IO) {
            val fileName = "$userId/avatar.jpg"
            val bucket = supabase.storage.from("avatars")

            // 1. Bild hochladen (upsert = true ersetzt das alte Bild)
            bucket.upload(fileName, imageBytes) {
                upsert = true
            }

            // 2. Öffentliche URL abrufen
            val url = bucket.publicUrl(fileName)

            // 3. URL im Profil speichern
            // Wir holen das aktuelle Profil und nutzen upsert, da dies bereits 
            // in upsertProfile funktioniert und stabiler gegen RLS-Nuancen ist
            val currentProfile = getProfile(userId)
            if (currentProfile != null) {
                upsertProfile(currentProfile.copy(avatarUrl = url))
            } else {
                // Fallback falls Profil noch nicht existiert (sollte nicht passieren)
                supabase.from("profiles").update({
                    set("avatar_url", url)
                }) {
                    filter { eq("id", userId) }
                }
            }

            url
        }

    suspend fun isUsernameTaken(username: String): Boolean = withContext(Dispatchers.IO) {
        val result = supabase.from("profiles")
            .select {
                filter {
                    eq("user_name", username)
                }
            }.decodeList<Profile>()
        return@withContext result.isNotEmpty()
    }
}
