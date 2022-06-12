package com.example.clonegramtestproject.data
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
class CountriesCodes(
    val name: String,
    @SerializedName("dial_code")
    val dialCode: String,
    val code: String,
) : Parcelable