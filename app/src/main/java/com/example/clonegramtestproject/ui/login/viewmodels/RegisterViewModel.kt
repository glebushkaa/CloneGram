package com.example.clonegramtestproject.ui.login.viewmodels

import androidx.lifecycle.ViewModel
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.models.TokenModel
import com.example.clonegramtestproject.data.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeNewUser
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterViewModel(
    private val rtNewUser: RealtimeNewUser,
    private val rtUser: RealtimeUser,
    private val cmHelper: CMHelper
) : ViewModel() {

    var phoneNumber: String? = null
    var username: String? = null
    var isRegisterCompleted = false

    private val auth = FirebaseAuth.getInstance()

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
                TokenModel(
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