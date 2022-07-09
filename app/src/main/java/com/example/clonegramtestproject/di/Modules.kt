package com.example.clonegramtestproject.di

import com.example.clonegramtestproject.data.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeGetter
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeMessage
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeNewUser
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.data.firebase.storage.StorageOperator
import com.example.clonegramtestproject.ui.message.viewmodels.DirectMessageViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel{
        DirectMessageViewModel(get(),get())
    }
}

val firebaseModule = module{
    factory { RealtimeGetter() }
    factory { RealtimeMessage() }
    factory { RealtimeNewUser() }
    factory { RealtimeUser() }
    factory { StorageOperator() }
    factory { CMHelper() }
}

