package com.example.clonegramtestproject.ui.start.viewmodel

import androidx.lifecycle.ViewModel
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.models.TokenModel
import com.example.clonegramtestproject.data.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeGetter
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StartViewModel(
    private val auth : FirebaseAuth,
    private val rtGetter: RealtimeGetter,
    private val rtUser : RealtimeUser,
    private val cmHelper : CMHelper
) : ViewModel() {

    var user: CommonModel? = null

    suspend fun getUser() {
        user = rtGetter.getUser(auth.uid.orEmpty())
    }

    suspend fun setToken() {
        cmHelper.getToken().let {
            rtUser.setUserToken(
                TokenModel(
                    it,
                    System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun clearOldTokens(user: CommonModel?) {
        user?.let {
            rtUser.clearOldTokens(it)
        }
    }

    suspend fun deleteUser() = rtUser.deleteUser()

    suspend fun checkLogin() = suspendCoroutine<Boolean> { emitter ->
        auth.currentUser?.let {
            emitter.resume(false)
        } ?: run {
            emitter.resume(true)
        }
    }

}