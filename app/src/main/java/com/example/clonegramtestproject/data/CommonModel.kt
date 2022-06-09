package com.example.clonegramtestproject.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class CommonModel(
    val username : String? = null,
    val phone : String? = null,
    val lastMessage : Map<String,LastMessageData>? = null,
    val chatUID : String? = null,
    val uidArray : ArrayList<String?>? = null,
    val permissionUidArray : Map<String,Boolean>? = null,
    val uid : String? = null,
    val singleChat : Boolean? = null,
    val userPicture : String? = null,
    val tokens : HashMap<String?,TokenData?>? = null
) : Parcelable