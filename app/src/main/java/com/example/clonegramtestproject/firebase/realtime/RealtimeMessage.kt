package com.example.clonegramtestproject.firebase.realtime

import com.example.clonegramtestproject.data.LastMessageData
import com.example.clonegramtestproject.data.MessageData
import com.example.clonegramtestproject.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealtimeMessage {

    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(USERS_MESSAGES_NODE)
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val currentUID = firebaseAuth.currentUser?.uid.orEmpty()

    suspend fun setLastMessage(
        uidArray: ArrayList<String>,
        chatUID: String,
        message: LastMessageData
    ) {
        withContext(Dispatchers.IO) {
            uidArray.forEach {
                databaseRefMessages
                    .child(chatUID)
                    .child(LAST_MESSAGE_NODE)
                    .child(it)
                    .setValue(message)
            }
        }
    }

    suspend fun changeLastMessage(
        chatUID: String,
        message: LastMessageData
    ) {
        withContext(Dispatchers.IO) {
            databaseRefMessages
                .child(chatUID)
                .child(LAST_MESSAGE_NODE)
                .child(currentUID)
                .setValue(message)
        }
    }

    suspend fun setSeenParameter(
        messageData: ArrayList<MessageData>,
        chatUID: String
    ) = withContext(Dispatchers.IO){
        if (messageData.isNotEmpty()) {
            messageData.forEach {
                if (it.uid != currentUID
                    && it.seen == null
                ) {
                    databaseRefMessages
                        .child(chatUID)
                        .child(MESSAGES_NODE)
                        .child(it.messageUid.orEmpty())
                        .child(SEEN_NODE)
                        .setValue(arrayListOf(currentUID))
                }
            }
        }
    }

    suspend fun sendMessage(
        userUID: String,
        messageData: MessageData,
        chatUID: String
    ) {
        withContext(Dispatchers.IO) {
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

    private suspend fun checkIsUserDeletedChat(userUID: String, messageUID: String) {
        withContext(Dispatchers.IO) {
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
    }

    suspend fun editMessage(text: String, chatUID: String, messageUID: String) {
        withContext(Dispatchers.IO) {
            databaseRefMessages
                .child(chatUID)
                .child(MESSAGES_NODE)
                .child(messageUID)
                .child(MESSAGE_NODE)
                .setValue(text)
        }
    }

    suspend fun deleteMessage(chatUID: String, messageUID: String) {
        withContext(Dispatchers.IO) {
            databaseRefMessages
                .child(chatUID)
                .child(MESSAGES_NODE)
                .child(messageUID)
                .removeValue()
        }
    }

    suspend fun deleteMessageForMe(chatUID: String, messageUID: String) {
        withContext(Dispatchers.IO) {
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
    }


    suspend fun deleteChat(messageUID: String) {
        withContext(Dispatchers.IO) {
            databaseRefMessages
                .child(messageUID)
                .removeValue()
        }
    }
}