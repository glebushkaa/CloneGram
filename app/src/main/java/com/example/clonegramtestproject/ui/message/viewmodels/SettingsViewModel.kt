package com.example.clonegramtestproject.ui.message.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clonegramtestproject.data.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.data.firebase.storage.StorageOperator
import com.example.clonegramtestproject.data.models.CommonModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val sOperator: StorageOperator,
    private val rtUser: RealtimeUser,
    private val cmHelper: CMHelper
) : ViewModel() {

    private val currentUser = FirebaseAuth.getInstance().currentUser

    var phoneNumber = currentUser?.phoneNumber
    var user: CommonModel? = null
    var username: String? = null

    suspend fun pushUserPicture(uri: Uri) = sOperator.pushUserPicture(uri)

    fun setPremiumIcon(icon : Int){
        viewModelScope.launch(Dispatchers.IO){
            rtUser.setPremiumIcon(icon)
        }
    }

    fun changeBio(bio : String){
        viewModelScope.launch(Dispatchers.IO){
            rtUser.changeBio(bio)
        }
    }

    fun changeUsername() {
        viewModelScope.launch(Dispatchers.IO) {
            rtUser.changeUsername(username)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            deleteToken()
            rtUser.signOut()
        }
    }

    fun deleteUser() {
        viewModelScope.launch(Dispatchers.IO) {
            rtUser.deleteUser()
        }
    }

    private suspend fun deleteToken() = withContext(Dispatchers.IO) {
        cmHelper.getToken().let {
            rtUser.deleteToken(it)
        }
    }

}