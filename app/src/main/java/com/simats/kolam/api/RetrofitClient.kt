package com.simats.kolam.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val BASE_URL = "http://192.168.111.148:5000/"

    val apiService: KolamApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KolamApiService::class.java)
    }
}
