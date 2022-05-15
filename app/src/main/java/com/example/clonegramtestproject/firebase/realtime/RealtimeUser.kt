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

class RealtimeUser {

    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(USERS_MESSAGES_NODE)
    private val databaseRefUsers = firebaseDatabase.getReference(USERS_NODE)
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val currentUID = firebaseAuth.currentUser?.uid.orEmpty()

    fun setUserPicture(userPictureLink: String) {
        databaseRefUsers
            .child(currentUID)
            .child(USER_PICTURE_NODE)
            .setValue(userPictureLink)
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

    private fun setNewPermissionUidArray(chatUID: String) {
        databaseRefMessages
            .child(chatUID)
            .child(PERMISSION_UID_ARRAY_NODE)
            .child(currentUID)
            .setValue(false)
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

    fun signOut(){
        firebaseAuth.signOut()
    }

    fun deleteUser(){
        databaseRefUsers
            .child(currentUID).removeValue()
        firebaseAuth.currentUser?.delete()
        firebaseAuth.signOut()
    }
    fun changeUsername(username : String?){
        databaseRefUsers
            .child(currentUID)
            .updateChildren(
                mapOf(USERNAME_NODE to username)
            )
    }

}