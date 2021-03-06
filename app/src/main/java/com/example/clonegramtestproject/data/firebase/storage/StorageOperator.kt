package com.example.clonegramtestproject.data.firebase.storage

import android.net.Uri
import android.util.Log
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeMessage
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.utils.CHATS_PICTURES_NODE
import com.example.clonegramtestproject.utils.USERS_PICTURES_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class StorageOperator(
    private val rtUser: RealtimeUser,
    storageRef : StorageReference
) {
    private val chatsPicturesRef = storageRef.child(CHATS_PICTURES_NODE)
    private val usersIconsRef = storageRef.child(USERS_PICTURES_NODE)

    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val currentUID = currentUser?.uid

    suspend fun pushUserPicture(uri: Uri) = suspendCoroutine<Unit> { emmiter ->
        usersIconsRef.child(currentUID.orEmpty())
            .child(uri.lastPathSegment.toString())
            .putFile(uri)
            .addOnSuccessListener {
                it.metadata?.reference?.downloadUrl
                    ?.addOnSuccessListener { userPicture ->
                        CoroutineScope(Dispatchers.IO).launch {
                            rtUser
                                .setUserPicture(
                                    userPicture
                                        .toString()
                                )
                            emmiter.resume(Unit)
                        }

                    }

            }.addOnFailureListener {
                emmiter.resumeWithException(it)
            }
    }

    suspend fun pushMessagePicture(
        uri: Uri, chatUID: String
    ) = suspendCoroutine<String> { emitter ->
        chatsPicturesRef
            .child(chatUID)
            .child(uri.lastPathSegment.toString())
            .putFile(uri).addOnSuccessListener {
                it.metadata?.reference?.downloadUrl
                    ?.addOnSuccessListener { uri ->
                        emitter.resume(uri.toString())
                    }?.addOnFailureListener { exception ->
                        emitter.resumeWithException(exception)
                    }
            }
    }
}