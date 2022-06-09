package com.example.clonegramtestproject.data

import com.google.gson.annotations.SerializedName

class RequestNotification (
    @SerializedName("to")
    var to: String? = null,
    @SerializedName("notification")
    var notificationData: NotificationData? = null
)