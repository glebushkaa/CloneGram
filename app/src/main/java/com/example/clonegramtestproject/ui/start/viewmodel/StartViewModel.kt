package com.example.clonegramtestproject.ui.start.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.data.TokenData
import com.example.clonegramtestproject.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.firebase.realtime.RealtimeGetter
import com.example.clonegramtestproject.firebase.realtime.RealtimeUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StartViewModel : ViewModel() {

    var user: CommonModel? = null
    private val auth = FirebaseAuth.getInstance()

    private val rtGetter = RealtimeGetter()
    private val rtUser = RealtimeUser()
    private val cmHelper = CMHelper()

    suspend fun getUser(){
        user = rtGetter.getUser(auth.uid.orEmpty())
    }

    suspend fun setToken() {
        cmHelper.getToken().let {
            rtUser.setUserToken(
                TokenData(
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

}