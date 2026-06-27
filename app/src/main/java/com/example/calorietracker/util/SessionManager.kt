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
