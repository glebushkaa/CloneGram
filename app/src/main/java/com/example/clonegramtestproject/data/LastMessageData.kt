package com.example.clonegramtestproject.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class LastMessageData(
    val uid: String? = null,
    val message: String? = null,
    val timestamp: Long? = null
) : Parcelable