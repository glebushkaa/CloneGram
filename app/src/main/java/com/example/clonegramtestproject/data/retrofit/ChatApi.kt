package com.example.clonegramtestproject.data.retrofit

import com.example.clonegramtestproject.data.models.RequestModel
import com.example.clonegramtestproject.utils.KEY
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApi {

    @Headers(
        "Authorization: key=$KEY",
        "Content-Type:application/json"
    )
    @POST("fcm/send")
    suspend fun sendChatNotification(@Body requestModel: RequestModel?) : ResponseBody

}