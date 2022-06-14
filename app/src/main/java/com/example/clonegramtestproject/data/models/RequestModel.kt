package com.example.clonegramtestproject.data.models

import com.google.gson.annotations.SerializedName

class RequestModel (
    @SerializedName("to")
    var to: String? = null,
    @SerializedName("notification")
    var notificationModel: NotificationModel? = null
)