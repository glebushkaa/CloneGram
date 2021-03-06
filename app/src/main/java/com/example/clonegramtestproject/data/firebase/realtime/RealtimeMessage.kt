package com.example.clonegramtestproject.data.firebase.realtime

import com.example.clonegramtestproject.data.models.LastMessageModel
import com.example.clonegramtestproject.data.models.MessageModel
import com.example.clonegramtestproject.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealtimeMessage(
    firebaseDatabase: FirebaseDatabase
) {

    private val databaseRefMessages = firebaseDatabase.getReference(MESSAGES_NODE)

    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val currentUID = currentUser?.uid.orEmpty()

    suspend fun setLastMessage(
        uidArray: ArrayList<String>,
        chatUID: String,
        message: LastMessageModel
    ) {
        withContext(Dispatchers.IO) {
            uidArray.forEach { uid ->
                databaseRefMessages
                    .child(chatUID)
                    .child(LAST_MESSAGE_NODE)
                    .child(uid)
                    .setValue(message)
            }
        }
    }

    suspend fun changeLastMessage(
        chatUID: String,
        message: LastMessageModel
    ) {
        withContext(Dispatchers.IO) {
            databaseRefMessages
                .child(chatUID)
                .child(LAST_MESSAGE_NODE)
                .child(currentUID)
                .setValue(message)
        }
    }

    fun setSeenParameter(
        messageData: ArrayList<MessageModel>,
        chatUID: String
    ) {
        if (messageData.isNotEmpty()) {
            messageData.forEach {
                if (it.uid != currentUID
                    && it.seen == null
                ) {
                    databaseRefMessages
                        .child(chatUID)
                        .child(DIALOGUES_NODE)
                        .child(it.messageUid.orEmpty())
                        .child(SEEN_NODE)
                        .setValue(arrayListOf(currentUID))
                }
            }
        }

    }

    suspend fun sendMessage(
        userUID: String,
        messageModel: MessageModel,
        chatUID: String
    ) {
        withContext(Dispatchers.IO) {
            databaseRefMessages
                .child(chatUID)
                .child(DIALOGUES_NODE)
                .push()
                .let {
                    it.setValue(
                        MessageModel(
                            uid = messageModel.uid,
                            username = messageModel.username,
                            message = messageModel.message,
                            timestamp = messageModel.timestamp,
                            permission = messageModel.permission,
                            messageUid = it.key,
                            picture = messageModel.picture
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
                .child(PERMISSION_LIST_NODE)
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
                .child(DIALOGUES_NODE)
                .child(messageUID)
                .child(MESSAGE_NODE)
                .setValue(text)
        }
    }

    suspend fun deleteMessage(chatUID: String, messageUID: String) {
        withContext(Dispatchers.IO) {
            databaseRefMessages
                .child(chatUID)
                .child(DIALOGUES_NODE)
                .child(messageUID)
                .removeValue()
        }
    }

    suspend fun deleteMessageForMe(chatUID: String, messageUID: String) {
        withContext(Dispatchers.IO) {
            databaseRefMessages
                .child(chatUID)
                .child(DIALOGUES_NODE)
                .child(messageUID)
                .child(PERMISSION_NODE)
                .apply {
                    get().addOnSuccessListener { snapshot ->
                        val uidList = ArrayList<String>()
                        snapshot.children.forEach {
                            val value = it.value as String?
                            if (value != currentUID) {
                                uidList.add(value.orEmpty())
                            }
                        }
                        setValue(uidList)
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