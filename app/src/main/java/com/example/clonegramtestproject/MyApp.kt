package com.example.clonegramtestproject

import android.app.Application
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApp : Application() {
    var retrofit: Retrofit? = null
    private val BASE_URL = "https://fcm.googleapis.com/"

    override fun onCreate() {
        super.onCreate()
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}