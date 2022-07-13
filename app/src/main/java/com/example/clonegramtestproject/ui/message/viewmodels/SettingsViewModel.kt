package com.example.clonegramtestproject.ui.message.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clonegramtestproject.data.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.data.firebase.storage.StorageOperator
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.sharedPrefs.SharedPrefsHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject

class SettingsViewModel(
    private val sOperator: StorageOperator,
    private val rtUser: RealtimeUser,
    private val cmHelper: CMHelper
) : ViewModel() {

    private val currentUser = FirebaseAuth.getInstance().currentUser

    var phoneNumber = currentUser?.phoneNumber
    var user: CommonModel? = null
    var username: String? = null

    fun pushUserPicture(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO){
            sOperator.pushUserPicture(uri)
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