package com.example.clonegramtestproject.firebase.cloudMessaging

import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CMHelper {

    suspend fun getToken() = withContext(Dispatchers.IO) {
        suspendCoroutine<String> { emitter ->
            FirebaseMessaging
                .getInstance()
                .token
                .addOnCompleteListener { // add error handling
                    if (!it.isSuccessful) {
                        emitter.resumeWithException(NullPointerException())
                        return@addOnCompleteListener
                    }
                    emitter.resume(it.result)
                }
        }
    }

}