package com.example.clonegramtestproject.data.firebase.realtime

import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.utils.USERNAME_NODE
import com.example.clonegramtestproject.utils.USERS_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RealtimeGetter(
    firebaseDatabase : FirebaseDatabase,
){

    private val auth = FirebaseAuth.getInstance()
    private val databaseRefUsers = firebaseDatabase.getReference(USERS_NODE)

    suspend fun getAllUsersList() = withContext(Dispatchers.IO) {
        suspendCoroutine<ArrayList<CommonModel>> { emitter ->
            databaseRefUsers.get().addOnSuccessListener {
                val allUsersList = ArrayList<CommonModel>()
                for (user in it.children) {
                    user.getValue(CommonModel::class.java)?.let { info ->
                        if (info.uid != this@RealtimeGetter.auth.currentUser?.uid) {
                            allUsersList.add(info)
                        }
                    }
                }
                emitter.resume(allUsersList)
            }.addOnFailureListener {
                emitter.resumeWithException(it)
            }
        }
    }

    suspend fun getUsername(uid: String) = withContext(Dispatchers.IO) {
        suspendCoroutine<String?> { emitter ->
            databaseRefUsers
                .child(uid)
                .child(USERNAME_NODE)
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    dataSnapshot.value?.let {
                        emitter.resume(it.toString())
                    } ?: run {
                        emitter.resume(null)
                    }
                }.addOnFailureListener {
                    emitter.resumeWithException(it)
                }
        }
    }

    suspend fun getUser(uid: String) = withContext(Dispatchers.IO) {
        suspendCoroutine<CommonModel?> { emitter ->
            databaseRefUsers
                .child(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val user: CommonModel? = snapshot
                        .getValue(CommonModel::class.java)
                    emitter.resume(user)
                }.addOnFailureListener { exception ->
                    emitter.resumeWithException(exception)
                }
        }
    }
}