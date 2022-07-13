package com.example.clonegramtestproject.data.firebase.realtime

import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.models.LastMessageModel
import com.example.clonegramtestproject.utils.MESSAGES_NODE
import com.example.clonegramtestproject.utils.PERMISSION_LIST_NODE
import com.example.clonegramtestproject.utils.USERS_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RealtimeNewUser(
    firebaseDatabase: FirebaseDatabase
) {

    private val databaseRefMessages = firebaseDatabase.getReference(MESSAGES_NODE)

    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val currentUID = currentUser?.uid


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

    suspend fun addUserToChat(user: CommonModel): String? =
        withContext(Dispatchers.IO) {
            var key: String? = null

            checkExistsChatsWithUser(user)?.let {
                key = it.chatUID.toString()
                databaseRefMessages
                    .child(key.orEmpty())
                    .child(PERMISSION_LIST_NODE)
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
                            permissionList = mapOf(
                                currentUID.orEmpty() to true,
                                user.uid.orEmpty() to true
                            ),
                            chatUID = key,
                            lastMessage = mapOf(
                                currentUID.orEmpty() to LastMessageModel(
                                    message = ""
                                ),
                                user.uid.orEmpty() to LastMessageModel(
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