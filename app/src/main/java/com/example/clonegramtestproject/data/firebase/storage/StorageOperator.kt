package com.example.clonegramtestproject.data.firebase.storage

import android.net.Uri
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.models.MessageModel
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeMessage
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.utils.CHATS_PICTURES_NODE
import com.example.clonegramtestproject.utils.USERS_PICTURES_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class StorageOperator {
    private val storageRef = Firebase.storage.reference
    private val chatsPicturesRef = storageRef.child(CHATS_PICTURES_NODE)
    private val usersIconsRef = storageRef.child(USERS_PICTURES_NODE)

    private val currentUID = FirebaseAuth.getInstance().currentUser?.uid

    private val realtimeOperator = RealtimeMessage()
    private val realtimeChanger = RealtimeUser()

    suspend fun pushUserPicture(uri: Uri)  = suspendCoroutine<Unit> { emmiter->
        usersIconsRef.child(currentUID.orEmpty())
            .child(uri.lastPathSegment.toString())
            .putFile(uri)
            .addOnSuccessListener {
                it.metadata?.reference?.downloadUrl
                    ?.addOnSuccessListener { userPicture ->
                        CoroutineScope(Dispatchers.IO).launch {
                            realtimeChanger
                                .setUserPicture(
                                    userPicture
                                        .toString()
                                )
                            emmiter.resume(Unit)
                        }

                    }

            }.addOnFailureListener{
                emmiter.resumeWithException(it)
            }
    }

    fun pushMessagePicture(
        uri: Uri, user: CommonModel,
        username: String, chatUID: String
    ) {
            chatsPicturesRef
                .child(chatUID)
                .child(uri.lastPathSegment.toString())
                .putFile(uri).addOnSuccessListener {

                    it.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener { messagePicture ->
                            val messageModel = MessageModel(
                                uid = currentUID,
                                username = username,
                                message = messagePicture.toString(),
                                timestamp = System.currentTimeMillis(),
                                uidPermission = arrayListOf(
                                    currentUID.orEmpty(),
                                    user.uid.orEmpty()
                                ),
                                picture = true
                            )

                            CoroutineScope(Dispatchers.IO).launch {
                                realtimeOperator.sendMessage(
                                    user.uid.orEmpty(),
                                    messageModel,
                                    chatUID = chatUID
                                )
                            }
                        }
                }
        }
}