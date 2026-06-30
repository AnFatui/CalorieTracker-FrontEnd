package com.example.calorietracker.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FoodRecognitionApi {

    @Multipart
    @POST("recognize")
    suspend fun recognizeFood(
        @Header("X-User-Id") userId: String,
        @Part image: MultipartBody.Part
    ): Response<ResponseBody>
}