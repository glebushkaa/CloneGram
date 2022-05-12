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
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RealtimeChanger {

    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(USERS_MESSAGES_NODE)
    private val databaseRefUsers = firebaseDatabase.getReference(USERS_NODE)
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val currentUID = firebaseAuth.currentUser?.uid.orEmpty()


    suspend fun addUserToMessages(user: CommonModel) = suspendCoroutine<String?> { emitter ->
        var key: String? = null
        CoroutineScope(Dispatchers.IO).launch {

            checkExistsChatsWithUser(user)?.let {
                key = it.chatUID.toString()
                databaseRefMessages
                    .child(key.orEmpty())
                    .child(PERMISSION_UID_ARRAY_NODE)
                    .child(currentUID)
                    .setValue(true)
            } ?: run {
                key = addNewUserToMessages(user)
            }
            emitter.resume(key)
        }
    }

    fun setUserPicture(userPictureLink: String) {
        databaseRefUsers
            .child(currentUID)
            .child(USER_PICTURE_NODE)
            .setValue(userPictureLink)
    }

    private fun addNewUserToMessages(user: CommonModel): String? {
        val key: String?

        databaseRefMessages
            .push()
            .let {
                key = it.key

                it.setValue(
                    CommonModel(
                        singleChat = true,
                        uidArray = arrayListOf(
                            currentUID,
                            user.uid.orEmpty()
                        ),
                        permissionUidArray = mapOf(
                            currentUID to true,
                            user.uid.orEmpty() to true
                        ),
                        chatUID = key,
                        lastMessage = LastMessageData(
                            "",
                            ""
                        )
                    )
                )
            }
        return key
    }

    private suspend fun checkExistsChatsWithUser(user: CommonModel) =
        suspendCoroutine<CommonModel?> { emitter ->
            databaseRefMessages
                .get()
                .addOnSuccessListener { snapshot ->
                    var message: CommonModel? = null
                    if (snapshot.exists()) {
                        for (messageItem in snapshot.children) {
                            messageItem
                                .getValue(CommonModel::class.java)
                                .let { messagesItem ->
                                    if (messagesItem?.singleChat == true &&
                                        messagesItem.uidArray?.containsAll(
                                            arrayListOf(
                                                currentUID,
                                                user.uid.orEmpty()
                                            )
                                        ) == true
                                    ) {
                                        message = messagesItem

                                    }
                                }
                        }
                    }
                    emitter.resume(message)
                }.addOnFailureListener { exception ->
                    emitter.resumeWithException(exception)
                }
        }

    fun addNewUser(
        username: String,
        uid: String,
        phone: String
    ) {
        Firebase.database.reference.child(USERS_NODE)
            .child(uid)
            .setValue(
                CommonModel(
                    username = username,
                    phone = phone,
                    uid = uid
                )
            )
    }

    fun setLastMessageForUser(
        chatUID: String,
        message: LastMessageData
    ) {
        databaseRefMessages
            .child(chatUID)
            .child(LAST_MESSAGE_NODE)
            .setValue(message)
    }

    fun deleteChatForCurrentUser(chatUID: String) {
        CoroutineScope(Dispatchers.IO).launch {
            databaseRefMessages
                .child(chatUID)
                .child(MESSAGES_NODE)
                .setValue(setNewMessagesPermissions(chatUID))
                .addOnSuccessListener {
                    setNewPermissionUidArray(chatUID)
                }
        }
    }

    private suspend fun setNewMessagesPermissions(chatUID: String) =
        suspendCoroutine<ArrayList<MessageData>> { emitter ->
            databaseRefMessages
                .child(chatUID)
                .child(MESSAGES_NODE)
                .let {
                    it.get()
                        .addOnSuccessListener { messageSnapshot ->
                            val messageArray = ArrayList<MessageData>()

                            if (messageSnapshot.exists()) {
                                for (message in messageSnapshot.children) {
                                    val newMessage = message.getValue(MessageData::class.java)
                                    newMessage?.uidPermission?.remove(currentUID)
                                    newMessage?.let { messageItem ->
                                        messageArray.add(messageItem)
                                    }
                                }
                            }
                            emitter.resume(messageArray)
                        }.addOnFailureListener { exception ->
                            emitter.resumeWithException(exception)
                        }
                }
        }

    private fun setNewPermissionUidArray(chatUID: String) {
        databaseRefMessages
            .child(chatUID)
            .child(PERMISSION_UID_ARRAY_NODE)
            .child(currentUID)
            .setValue(false)
    }

}