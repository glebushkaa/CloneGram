package com.example.clonegramtestproject.data.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
class CodesModel(
    val name: String,
    val dial_code: String,
    val code: String,
) : Parcelable