package com.example.clonegramtestproject.data.firebase.realtime

import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.models.MessageModel
import com.example.clonegramtestproject.data.models.TokenModel
import com.example.clonegramtestproject.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealtimeUser(
    firebaseDatabase: FirebaseDatabase,
    private val currentUser: FirebaseUser?,
    private val auth: FirebaseAuth
) {

    private val databaseRefMessages = firebaseDatabase.getReference(MESSAGES_NODE)
    private val databaseRefUsers = firebaseDatabase.getReference(USERS_NODE)
    private val currentUID = currentUser?.uid

    companion object {
        const val tenDays = 864000000
    }

    suspend fun setUserPicture(userPictureLink: String) {
        withContext(Dispatchers.IO) {
            databaseRefUsers
                .child(
                    currentUser?.uid.orEmpty()
                )
                .child(PICTURE_NODE)
                .setValue(userPictureLink)
        }
    }

    suspend fun deleteChatForCurrentUser(chatUID: String) {
        withContext(Dispatchers.IO) {
            setNewMessagesPermissions(chatUID)
            databaseRefMessages
                .child(chatUID)
                .child(PERMISSION_LIST_NODE)
                .child(currentUID.orEmpty())
                .setValue(false)
        }
    }

    private suspend fun setNewMessagesPermissions(chatUID: String) =
        withContext(Dispatchers.IO) {
            databaseRefMessages
                .child(chatUID)
                .child(DIALOGUES_NODE)
                .let { ref ->
                    ref.get()
                        .addOnSuccessListener { messageSnapshot ->
                            val messageArray = ArrayList<MessageModel>()
                            if (messageSnapshot.exists()) {
                                messageSnapshot.children.forEach { message ->
                                    val newMessage = message.getValue(MessageModel::class.java)
                                    newMessage?.permission?.remove(
                                        currentUID
                                    )
                                    newMessage?.let { messageModel -> messageArray.add(messageModel) }

                                }
                            }
                            ref.setValue(messageArray)
                        }
                }
        }

    suspend fun setUserToken(token: TokenModel) {
        withContext(Dispatchers.IO) {
            databaseRefUsers
                .child(currentUser?.uid.orEmpty())
                .child(TOKEN_NODE)
                .child(token.token.orEmpty())
                .setValue(token)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun deleteUser() {
        withContext(Dispatchers.IO) {
            databaseRefUsers
                .child(currentUser?.uid.orEmpty())
                .removeValue()
                .addOnSuccessListener {
                    currentUser?.delete()
                    auth.signOut()
                }
        }
    }

    suspend fun changeUsername(username: String?) {
        withContext(Dispatchers.IO) {
            databaseRefUsers
                .child(currentUser?.uid.orEmpty())
                .updateChildren(
                    mapOf(USERNAME_NODE to username)
                )
        }
    }

    suspend fun deleteToken(token: String) {
        withContext(Dispatchers.IO) {
            databaseRefUsers
                .child(currentUser?.uid.orEmpty())
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