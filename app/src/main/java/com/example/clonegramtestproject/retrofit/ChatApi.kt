package com.example.clonegramtestproject.retrofit

import com.example.clonegramtestproject.data.RequestNotification
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApi {

    @Headers(
        "Authorization: key=AAAAy-kQMIc:APA91bFi9u2AsaMEDbdHTrfZKtb4vqEIaHtCJSBu5e-8tN8RmSLVvwBPzHPsANl2sG923obUa1oVEXhnTGrvLgvsgHE05BzpLZsjjO4Khhv6OodbmteKJhOjNCe6_P--Flt_q4DWE0ec",
        "Content-Type:application/json"
    )
    @POST("fcm/send")
    fun sendChatNotification(@Body requestNotification: RequestNotification?):
            Call<ResponseBody> // coroutines

}