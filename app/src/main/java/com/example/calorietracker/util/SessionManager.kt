package com.example.calorietracker.util

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SessionManager(
    private val supabase: SupabaseClient
) {
    /**
     * Suspends until Supabase has finished restoring (or failing to restore) the persisted
     * session. [isLoggedIn] and [currentUserId] read in-memory auth state that is only
     * populated once this completes, so callers must await this before trusting either on
     * a cold start (e.g. after the process was killed in the background).
     */
    suspend fun awaitInitialization() {
        supabase.auth.awaitInitialization()
    }

    suspend fun logout() {
        supabase.auth.signOut()
    }

    suspend fun login(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String, data: JsonObject? = null) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = data
        }
    }

    /**
     * Signs in with a Google ID token. Supabase creates the user on first call, so this
     * covers both login and registration via Google.
     */
    suspend fun signInWithGoogle(idToken: String, displayName: String? = null) {
        supabase.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
        }

        // On first sign-in with a fresh Supabase user, seed display_name from the Google
        // account so the onboarding name field can be pre-filled instead of colliding with
        // an already-taken username the user has to type themselves.
        val hasDisplayName = supabase.auth.currentUserOrNull()?.userMetadata?.get("display_name") != null
        if (!displayName.isNullOrBlank() && !hasDisplayName) {
            supabase.auth.updateUser {
                data = buildJsonObject { put("display_name", displayName) }
            }
        }
    }

    val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id

    val currentSession: UserSession?
        get() = supabase.auth.currentSessionOrNull()

    val currentUserInfo: UserInfo?
        get() = supabase.auth.currentUserOrNull()

    val isLoggedIn: Boolean
        get() = currentUserId != null
}
