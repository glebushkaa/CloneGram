package com.example.clonegramtestproject.data.retrofit

import com.example.clonegramtestproject.data.models.NotificationModel
import com.example.clonegramtestproject.data.models.RequestModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class RetrofitHelper {


    fun sendNotification(
        retrofit : Retrofit,
        token: String?,
        notificationModel: NotificationModel?,
    ) {
        val requestModel = RequestModel(
            token, notificationModel
        )
        val apiService = retrofit.create(ChatApi::class.java)
        val responseBodyCall: Call<ResponseBody> =
            apiService.sendChatNotification(requestModel)
        responseBodyCall.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>,
                                    response: Response<ResponseBody?>) {}
            override fun onFailure(call: Call<ResponseBody?>,
                                   t: Throwable) {}
        })
    }

}