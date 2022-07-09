package com.example.clonegramtestproject

import android.app.Application
import com.example.clonegramtestproject.di.firebaseModule
import com.example.clonegramtestproject.di.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
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
        startKoin {
            androidContext(this@MyApp)
            modules(listOf(viewModelsModule, firebaseModule))
        }
    }
}