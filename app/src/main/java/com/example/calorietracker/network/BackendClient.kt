package com.example.calorietracker.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object BackendClient {


    private const val BASE_URL =
        "http://138.68.152.93/"

    private val okHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build()

    val api: FoodRecognitionApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(FoodRecognitionApi::class.java)
    }
}