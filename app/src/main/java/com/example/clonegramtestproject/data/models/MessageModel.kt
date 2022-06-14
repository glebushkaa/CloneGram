package com.example.clonegramtestproject.data.models


class MessageModel(
    val uid : String? = null,
    val username : String? = null,
    val message : String? = null,
    val timestamp : Long? = null,
    val uidPermission : ArrayList<String>? = null,
    val messageUid : String? = null,
    val seen : ArrayList<String>? = null,
    val picture : Boolean = false
)
