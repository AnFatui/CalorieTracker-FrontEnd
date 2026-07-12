package com.example.calorietracker.util

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.JsonObject

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

    val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id

    val currentSession: UserSession?
        get() = supabase.auth.currentSessionOrNull()

    val currentUserInfo: UserInfo?
        get() = supabase.auth.currentUserOrNull()

    val isLoggedIn: Boolean
        get() = currentUserId != null
}
