package com.example.clonegramtestproject.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class TokenModel (
    val token : String? = null,
    val timestamp: Long? = null
) : Parcelable