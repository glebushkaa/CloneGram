package com.example.clonegramtestproject

import android.app.Application
import android.util.Log
import com.example.clonegramtestproject.data.RequestNotification
import com.example.clonegramtestproject.data.NotificationData
import com.example.clonegramtestproject.retrofit.ChatApi
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApp : Application() {

    private var retrofit: Retrofit? = null // use Koin di
    private val BASE_URL = "https://fcm.googleapis.com/"
//    private var apiService : ChatApi? = null

    override fun onCreate() {
        super.onCreate()
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun sendNotification( // bad location method
        token: String?,
        notificationData: NotificationData?,
    ) {
        val requestNotification = RequestNotification(
            token, notificationData
        )
        val apiService = retrofit?.create(ChatApi::class.java)
        val responseBodyCall: Call<ResponseBody>? =
            apiService?.sendChatNotification(requestNotification)
        responseBodyCall?.enqueue(object : Callback<ResponseBody?> { // coroutines
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Log.d("RESPONSE", response.message())
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.d("FAILURE", t.message.orEmpty()) // handle error keys
            }

        })
    }
}