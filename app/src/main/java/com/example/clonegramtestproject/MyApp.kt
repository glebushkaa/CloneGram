package com.example.clonegramtestproject

import android.app.Application
import com.example.clonegramtestproject.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            modules(
                listOf(
                    activityViewModelModule,
                    startViewModelModule,
                    loginViewModelModule,
                    messageViewModelsModule,
                    retrofitModule,
                    apiModule,
                    languageModule,
                    firebaseModule,
                    animationsModule,
                    sharedPrefsModule
                )
            )
        }

    }
}