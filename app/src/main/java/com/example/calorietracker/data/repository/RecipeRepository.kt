package com.example.calorietracker.data.repository

import com.example.calorietracker.data.model.Recipe
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository(
    private val supabase: SupabaseClient
) {

    suspend fun getRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        supabase.from("recipes")
            .select()
            .decodeList<Recipe>()
    }

    suspend fun getRecipeById(recipeId: String): Recipe? = withContext(Dispatchers.IO) {
        supabase.from("recipes")
            .select {
                filter {
                    eq("id", recipeId)
                }
            }.decodeSingleOrNull<Recipe>()
    }
}
