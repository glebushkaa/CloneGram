package com.example.clonegramtestproject.di

import com.example.clonegramtestproject.data.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeGetter
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeMessage
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeNewUser
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.data.firebase.storage.StorageOperator
import com.example.clonegramtestproject.data.language.LanguageHelper
import com.example.clonegramtestproject.data.retrofit.ChatApi
import com.example.clonegramtestproject.data.retrofit.RetrofitHelper
import com.example.clonegramtestproject.data.sharedPrefs.SharedPrefsHelper
import com.example.clonegramtestproject.ui.Animations
import com.example.clonegramtestproject.ui.activity.viewmodel.ActivityViewModel
import com.example.clonegramtestproject.ui.login.viewmodels.CountryViewModel
import com.example.clonegramtestproject.ui.login.viewmodels.LoginViewModel
import com.example.clonegramtestproject.ui.login.viewmodels.RegisterViewModel
import com.example.clonegramtestproject.ui.login.viewmodels.VerifyViewModel
import com.example.clonegramtestproject.ui.message.viewmodels.DirectMessageViewModel
import com.example.clonegramtestproject.ui.message.viewmodels.FindNewUserViewModel
import com.example.clonegramtestproject.ui.message.viewmodels.GeneralMessageViewModel
import com.example.clonegramtestproject.ui.message.viewmodels.SettingsViewModel
import com.example.clonegramtestproject.ui.start.viewmodel.StartViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val FCM_BASE_URL = "https://fcm.googleapis.com/"

val activityViewModelModule = module {
    viewModel{
        ActivityViewModel(get())
    }
}

val startViewModelModule = module {
    viewModel{
        StartViewModel(get(),get(),get(),get())
    }
}

val loginViewModelModule = module {

    viewModel{
        LoginViewModel()
    }
    viewModel {
        RegisterViewModel(get(),get(),get(),get())
    }
    viewModel{
        VerifyViewModel(get(),get(),get(),get())
    }
    viewModel{
        CountryViewModel()
    }
}

val messageViewModelsModule = module {

    viewModel {
        DirectMessageViewModel(get(), get(),get(),get(),get())
    }
    viewModel{
        GeneralMessageViewModel(get(),get(),get(),get(),get())
    }
    viewModel{
        FindNewUserViewModel(get())
    }
    viewModel {
        SettingsViewModel(get(),get(),get(),get())
    }
}

val apiModule = module {

    fun provideApi(retrofit: Retrofit) = retrofit.create(ChatApi::class.java)

    single { provideApi(get()) }

}

val retrofitModule = module {

    fun provideRetrofit() = Retrofit.Builder()
        .baseUrl(FCM_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    single { provideRetrofit() }

    single { RetrofitHelper(get()) }

}

val animationsModule = module{

    single { Animations() }

}

val languageModule = module {

    single { LanguageHelper() }

}

val sharedPrefsModule = module {

    single { SharedPrefsHelper() }

}

val firebaseModule = module {

    fun provideAuth() = FirebaseAuth.getInstance()

    fun provideCurrentUser() = FirebaseAuth.getInstance().currentUser

    fun provideDatabase() = Firebase.database

    fun provideStorageRef() = Firebase.storage.reference

    factory { provideStorageRef() }

    factory { provideAuth() }

    factory { provideDatabase() }

    factory { provideCurrentUser() }

    factory { RealtimeGetter(get(),get()) }
    factory { RealtimeMessage(get(),get()) }
    factory { RealtimeNewUser(get(),get()) }
    factory { RealtimeUser(get(),get(),get()) }

    factory { StorageOperator(get(),get(),get(),get()) }

    factory { CMHelper() }

}

