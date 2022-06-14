package com.example.clonegramtestproject.ui.login.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.data.TokenData
import com.example.clonegramtestproject.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.firebase.realtime.RealtimeNewUser
import com.example.clonegramtestproject.firebase.realtime.RealtimeUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterViewModel : ViewModel() {

    val auth = FirebaseAuth.getInstance()

    var phoneNumber: String? = null
    var username: String? = null
    var isRegisterCompleted = false

    private val rtNewUser = RealtimeNewUser()
    private val rtUser = RealtimeUser()
    private val cmHelper = CMHelper()

    suspend fun addNewUser() {
        withContext(Dispatchers.IO) {
            rtNewUser.addNewUser(
                CommonModel(
                    username = username.orEmpty(),
                    uid = auth.currentUser?.uid.orEmpty(),
                    phone = phoneNumber.orEmpty()
                )
            )
            rtUser.setUserToken(
                TokenData(
                    cmHelper.getToken(),
                    System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteUser() {
        auth.currentUser?.delete()!!
        auth.signOut()
    }

}