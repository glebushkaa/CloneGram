package com.example.clonegramtestproject.data.retrofit

import android.util.Log
import com.example.clonegramtestproject.data.models.NotificationModel
import com.example.clonegramtestproject.data.models.RequestModel

class RetrofitHelper(private val chatApi: ChatApi) {

    suspend fun sendNotification(
        token: String?,
        notificationModel: NotificationModel?,
    ) {
        chatApi.sendChatNotification(
            RequestModel(token, notificationModel)
        )
    }
}