package com.example.clonegramtestproject.firebase.realtime

import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.data.LastMessageData
import com.example.clonegramtestproject.utils.PERMISSION_UID_ARRAY_NODE
import com.example.clonegramtestproject.utils.USERS_MESSAGES_NODE
import com.example.clonegramtestproject.utils.USERS_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RealtimeNewUser {

    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(USERS_MESSAGES_NODE)
    private val currentUID = FirebaseAuth.getInstance().currentUser?.uid


    suspend fun addNewUser(
        user: CommonModel
    ) {
        withContext(Dispatchers.IO) {
            Firebase.database.reference.child(USERS_NODE)
                .child(user.uid.orEmpty())
                .setValue(
                    user
                )
        }
    }

    suspend fun addUserToMessages(user: CommonModel): String? =
        withContext(Dispatchers.IO) {

            var key: String? = null

            checkExistsChatsWithUser(user)?.let {
                key = it.chatUID.toString()
                databaseRefMessages
                    .child(key.orEmpty())
                    .child(PERMISSION_UID_ARRAY_NODE)
                    .child(currentUID.orEmpty())
                    .setValue(true)
            } ?: run {
                key = addNewUserToMessages(user)
            }
            return@withContext key
        }

    private suspend fun addNewUserToMessages(user: CommonModel): String? =
        withContext(Dispatchers.IO) {
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
                                currentUID.orEmpty() to true,
                                user.uid.orEmpty() to true
                            ),
                            chatUID = key,
                            lastMessage = mapOf(
                                currentUID.orEmpty() to LastMessageData(
                                    message = ""
                                ),
                                user.uid.orEmpty() to LastMessageData(
                                    message = ""
                                )
                            )
                        )
                    )
                }
            return@withContext key
        }

    private suspend fun checkExistsChatsWithUser(user: CommonModel) =
        withContext(Dispatchers.IO) {
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
        }

}