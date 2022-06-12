package com.example.clonegramtestproject.retrofit

import com.example.clonegramtestproject.data.RequestNotification
import com.example.clonegramtestproject.utils.KEY
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApi {

    @Headers(
        "Authorization: key=$KEY",
        "Content-Type:application/json"
    )
    @POST("fcm/send")
    fun sendChatNotification(@Body requestNotification: RequestNotification?):
            Call<ResponseBody>

}