package com.example.clonegramtestproject.firebase.realtime

import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.data.MessageData
import com.example.clonegramtestproject.data.TokenData
import com.example.clonegramtestproject.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RealtimeUser {

    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(USERS_MESSAGES_NODE)
    private val databaseRefUsers = firebaseDatabase.getReference(USERS_NODE)
    private val firebaseAuth = FirebaseAuth.getInstance()

    companion object {
        const val tenDays = 864000000
    }

    suspend fun setUserPicture(userPictureLink: String) {
        withContext(Dispatchers.IO) {
            databaseRefUsers
                .child(
                    firebaseAuth.currentUser
                        ?.uid.orEmpty()
                )
                .child(USER_PICTURE_NODE)
                .setValue(userPictureLink)
        }
    }

    suspend fun deleteChatForCurrentUser(chatUID: String) {
        withContext(Dispatchers.IO) {
            databaseRefMessages
                .child(chatUID)
                .child(MESSAGES_NODE)
                .setValue(setNewMessagesPermissions(chatUID))
                .addOnSuccessListener {
                    launch {
                        setNewPermissionUidArray(chatUID)
                    }
                }
        }
    }

    private suspend fun setNewPermissionUidArray(chatUID: String) {
        withContext(Dispatchers.IO) {
            databaseRefMessages
                .child(chatUID)
                .child(PERMISSION_UID_ARRAY_NODE)
                .child(firebaseAuth.currentUser?.uid.orEmpty())
                .setValue(false)
        }
    }

    private suspend fun setNewMessagesPermissions(chatUID: String) =
        withContext(Dispatchers.IO) {
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
                                        newMessage?.uidPermission?.remove(
                                            firebaseAuth.currentUser?.uid.orEmpty()
                                        )
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
        }

    suspend fun setUserToken(token: TokenData) {
        withContext(Dispatchers.IO) {
            databaseRefUsers
                .child(firebaseAuth.currentUser?.uid.orEmpty())
                .child(TOKEN_NODE)
                .child(token.token.orEmpty())
                .setValue(token)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun deleteUser() {
        withContext(Dispatchers.IO) {
            databaseRefUsers
                .child(firebaseAuth.currentUser?.uid.orEmpty())
                .removeValue()
                .addOnSuccessListener {
                    firebaseAuth.currentUser?.delete()
                    firebaseAuth.signOut()
                }
        }
    }

    suspend fun changeUsername(username: String?) {
        withContext(Dispatchers.IO) {
            databaseRefUsers
                .child(firebaseAuth.currentUser?.uid.orEmpty())
                .updateChildren(
                    mapOf(USERNAME_NODE to username)
                )
        }
    }

    suspend fun deleteToken(token: String) {
        withContext(Dispatchers.IO) {
            databaseRefUsers
                .child(firebaseAuth.currentUser?.uid.orEmpty())
                .child(TOKEN_NODE)
                .child(token)
                .removeValue()
        }
    }

    suspend fun clearOldTokens(user: CommonModel) {
        withContext(Dispatchers.IO) {
            user.tokens?.forEach {
                it.value?.timestamp?.let { timestamp ->
                    if (System.currentTimeMillis() - tenDays > timestamp) {
                        it.value?.token?.let { token ->
                            deleteToken(token)
                        }

                    }
                }
                return@forEach
            }
        }
    }

}