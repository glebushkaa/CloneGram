package com.example.clonegramtestproject.firebase.realtime

import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.data.LastMessageData
import com.example.clonegramtestproject.data.MessageData
import com.example.clonegramtestproject.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RealtimeMessage {

    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(USERS_MESSAGES_NODE)
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val currentUID = firebaseAuth.currentUser?.uid.orEmpty()

    fun setLastMessage(
        uidArray: ArrayList<String>,
        chatUID: String,
        message: LastMessageData
    ) {
        uidArray.forEach {
            databaseRefMessages
                .child(chatUID)
                .child(LAST_MESSAGE_NODE)
                .child(it)
                .setValue(message)
        }
    }

    fun changeLastMessage(
        chatUID: String,
        message: LastMessageData
    ) {
        databaseRefMessages
            .child(chatUID)
            .child(LAST_MESSAGE_NODE)
            .child(currentUID)
            .setValue(message)
    }

    fun sendMessage(
        userUID: String,
        messageData: MessageData,
        chatUID: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            databaseRefMessages
                .child(chatUID)
                .child(MESSAGES_NODE)
                .push()
                .let {
                    it.setValue(
                        MessageData(
                            uid = messageData.uid,
                            username = messageData.username,
                            message = messageData.message,
                            timestamp = messageData.timestamp,
                            uidPermission = messageData.uidPermission,
                            messageUid = it.key,
                            picture = messageData.picture
                        )
                    )
                }
            checkIsUserDeletedChat(userUID, chatUID)
        }
    }

    fun checkIsUserDeletedChat(userUID: String, messageUID: String) {
        databaseRefMessages
            .child(messageUID)
            .child(PERMISSION_UID_ARRAY_NODE)
            .child(userUID)
            .let {
                it.get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val permission = snapshot.value as Boolean
                            if (!permission) {
                                it.setValue(true)
                            }
                        }
                    }
            }
    }

    fun editMessage(text: String, chatUID: String, messageUID: String) {
        databaseRefMessages
            .child(chatUID)
            .child(MESSAGES_NODE)
            .child(messageUID)
            .child(MESSAGE_NODE)
            .setValue(text)
    }

    fun deleteMessage(chatUID: String, messageUID: String) {
        databaseRefMessages
            .child(chatUID)
            .child(MESSAGES_NODE)
            .child(messageUID)
            .removeValue()
    }

    fun deleteMessageForMe(chatUID: String, messageUID: String) {
        databaseRefMessages
            .child(chatUID)
            .child(MESSAGES_NODE)
            .child(messageUID)
            .child(UID_PERMISSION_NODE)
            .let {
                it.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val uidArray = ArrayList<String>()
                        for (uid in snapshot.children) {
                            (uid.value as String).let { uidItem ->
                                if (uidItem != currentUID) {
                                    uidArray.add(uidItem)
                                }
                            }
                        }
                        it.setValue(uidArray)
                    }
                }
            }
    }


    fun deleteChat(messageUID: String) {
        databaseRefMessages
            .child(messageUID)
            .removeValue()
    }
}