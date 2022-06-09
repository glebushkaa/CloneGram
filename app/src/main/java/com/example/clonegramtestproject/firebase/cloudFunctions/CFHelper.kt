package com.example.clonegramtestproject.firebase.cloudFunctions

import com.example.clonegramtestproject.utils.USERS_MESSAGES_NODE
import com.google.firebase.database.ktx.database
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

class CFHelper {
    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(USERS_MESSAGES_NODE)
    fun s() {
        databaseRefMessages
    }
}