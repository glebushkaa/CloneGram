package com.example.clonegramtestproject.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class LastMessageModel(
    val message: String? = null,
    val timestamp: Long? = null,
    val picture : Boolean = false
) : Parcelable