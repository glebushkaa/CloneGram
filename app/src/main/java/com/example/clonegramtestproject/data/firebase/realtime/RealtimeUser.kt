package com.example.clonegramtestproject.data.firebase.realtime

import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.models.MessageModel
import com.example.clonegramtestproject.data.models.TokenModel
import com.example.clonegramtestproject.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealtimeUser(
    firebaseDatabase: FirebaseDatabase,
) {
    private val auth = FirebaseAuth.getInstance()
    private val refMessages = firebaseDatabase.getReference(MESSAGES_NODE)
    private val refUsers = firebaseDatabase.getReference(USERS_NODE)

    private val currentUser = auth.currentUser
    private val currentUID = currentUser?.uid.orEmpty()

    companion object {
        const val tenDays = 864000000
    }

    fun changeBio(bio : String){
        refUsers
            .child(currentUID)
            .child(BIO_NODE)
            .setValue(bio)
    }

    fun setPremiumIcon(icon : Int){
        refUsers
            .child(currentUID)
            .child(ICON_NODE)
            .setValue(icon)
    }

    suspend fun setUserPicture(userPictureLink: String) {
        withContext(Dispatchers.IO) {
            refUsers
                .child(
                    currentUID
                )
                .child(PICTURE_NODE)
                .setValue(userPictureLink)
        }
    }

    suspend fun deleteChatForCurrentUser(chatUID: String) {
        withContext(Dispatchers.IO) {
            setNewMessagesPermissions(chatUID)
            refMessages
                .child(chatUID)
                .child(PERMISSION_LIST_NODE)
                .child(currentUID)
                .setValue(false)
        }
    }

    private suspend fun setNewMessagesPermissions(chatUID: String) =
        withContext(Dispatchers.IO) {
            refMessages
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
            currentUser?.let {
                refUsers
                    .child(it.uid)
                    .child(TOKEN_NODE)
                    .child(token.token.orEmpty())
                    .setValue(token)
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun deleteUser() {
        withContext(Dispatchers.IO) {
            refUsers
                .child(currentUID)
                .removeValue()
                .addOnSuccessListener {
                    currentUser?.delete()
                    auth.signOut()
                }
        }
    }

    suspend fun changeUsername(username: String?) {
        withContext(Dispatchers.IO) {
            refUsers
                .child(currentUID)
                .updateChildren(
                    mapOf(USERNAME_NODE to username)
                )
        }
    }

    suspend fun deleteToken(token: String) {
        withContext(Dispatchers.IO) {
            refUsers
                .child(currentUID)
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