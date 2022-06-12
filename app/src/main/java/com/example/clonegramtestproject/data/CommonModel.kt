package com.example.clonegramtestproject.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class CommonModel(
    val username: String? = null,
    val phone: String? = null,
    val lastMessage: Map<String, LastMessageData>? = null,
    val chatUID: String? = null,
    val uidArray: ArrayList<String?>? = null,
    val permissionUidArray: Map<String, Boolean>? = null,
    val uid: String? = null,
    val singleChat: Boolean? = null,
    val userPicture: String? = null,
    val tokens: HashMap<String?, TokenData?>? = null
) : Parcelable


//class CommonMapper() {
//
//    fun toEntity(model: CommonModel): CommonEntity {
//        with(model) {
//            return CommonEntity(
//                username!!
//            )
//        }
//    }
//
//}
//
//@Parcelize
//class CommonEntity(
//    val username: String,
//    val phone: String,
//    val lastMessage: Map<String, LastMessageData>? = null,
//    val chatUID: String? = null,
//    val uidArray: ArrayList<String?>? = null,
//    val permissionUidArray: Map<String, Boolean>,
//    val uid: String,
//    val singleChat: Boolean,
//    val userPicture: String? = null,
//    val tokens: HashMap<String?, TokenData?>
//) : Parcelable