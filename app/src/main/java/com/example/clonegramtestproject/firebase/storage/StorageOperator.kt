package com.example.clonegramtestproject.firebase.storage

import android.content.Context
import android.net.Uri
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.data.MessageData
import com.example.clonegramtestproject.firebase.realtime.RealtimeChanger
import com.example.clonegramtestproject.firebase.realtime.RealtimeOperator
import com.example.clonegramtestproject.utils.CHATS_PICTURES_NODE
import com.example.clonegramtestproject.utils.USERS_PICTURES_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class StorageOperator {
    private val storageRef = Firebase.storage.reference
    private val chatsPicturesRef = storageRef.child(CHATS_PICTURES_NODE)
    private val usersIconsRef = storageRef.child(USERS_PICTURES_NODE)

    private val currentUID = FirebaseAuth.getInstance().currentUser?.uid

    private val realtimeOperator = RealtimeOperator()
    private val realtimeChanger = RealtimeChanger()

    suspend fun pushUserPicture(uri: Uri) = suspendCoroutine<Unit> { emitter ->
        usersIconsRef.child(currentUID.orEmpty())
            .child(uri.lastPathSegment.toString())
            .putFile(uri)
            .addOnSuccessListener {
                it.metadata?.reference?.downloadUrl
                    ?.addOnSuccessListener { userPicture ->
                        realtimeChanger
                            .setUserPicture(
                                userPicture
                                    .toString()
                            )
                    }
                emitter.resume(Unit)
            }.addOnFailureListener { exception ->
                emitter.resumeWithException(exception)
            }
    }

    fun pushMessagePicture(
        uri: Uri, user: CommonModel,
        username: String, chatUID: String
    ) {
        chatsPicturesRef.child(uri.lastPathSegment.toString())
            .putFile(uri).addOnSuccessListener {

                it.metadata?.reference?.downloadUrl
                    ?.addOnSuccessListener { messagePicture ->
                        val messageData = MessageData(
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

                        realtimeOperator.sendMessage(
                            user.uid.orEmpty(),
                            messageData,
                            chatUID = chatUID
                        )
                    }
            }
    }
}