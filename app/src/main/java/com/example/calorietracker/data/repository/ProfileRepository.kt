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
        // 1. Profil aus der Tabelle laden
        val profile = supabase.from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<Profile>()

        // 2. Display Name aus den Auth-User-Metadaten holen
        val user = supabase.auth.currentUserOrNull()
        val displayName = user?.userMetadata?.get("display_name")?.jsonPrimitive?.content

        // 3. Profil mit dem Display Name aus Auth vervollständigen
        profile?.copy(displayName = displayName)
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

    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "$userId/avatar.jpg"
        val bucket = supabase.storage.from("avatars")

        // 1. Bild hochladen (upsert = true ersetzt das alte Bild)
        bucket.upload(fileName, imageBytes) {
            upsert = true
        }

        // 2. Öffentliche URL abrufen
        val url = bucket.publicUrl(fileName)

        // 3. URL im Profil in der DB speichern
        supabase.from("profiles").update({
            set("avatar_url", url)
        }) {
            filter { eq("id", userId) }
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
