package com.example.clonegramtestproject.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class TokenData (
    val token : String? = null,
    val timestamp: Long? = null
) : Parcelable