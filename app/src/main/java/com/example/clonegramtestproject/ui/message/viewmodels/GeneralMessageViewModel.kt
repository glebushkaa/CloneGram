package com.example.clonegramtestproject.ui.message.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeGetter
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeMessage
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.utils.MESSAGES_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GeneralMessageViewModel : ViewModel() {
    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(MESSAGES_NODE)
    private var auth = FirebaseAuth.getInstance()

    val currentUID = FirebaseAuth.getInstance().currentUser?.uid
    var phoneNumber = auth.currentUser?.phoneNumber
    var username: String? = null
    var user: CommonModel? = null

    private var allMessagedUsersListener: ValueEventListener? = null
    val allMessagedUsersLiveData = MutableLiveData<List<CommonModel>>()

    var allUsersList = ArrayList<CommonModel>()
    private var chatsList = ArrayList<CommonModel>()
    var visibleDataList = ArrayList<CommonModel>()
    private var filteredUsersList = ArrayList<CommonModel>()
    var uidList = ArrayList<String?>()

    private val rtMessage = RealtimeMessage()
    private val rtUser = RealtimeUser()
    private val rtGetter = RealtimeGetter()


    suspend fun filterText(text: String?) = suspendCoroutine<ArrayList<CommonModel>> { emitter ->
        text?.let {
            filteredUsersList.clear()
            visibleDataList.forEach {
                if (it.username?.contains(text) == true &&
                    it.uid != currentUID
                ) {
                    filteredUsersList.add(it)
                }
            }
            emitter.resume(filteredUsersList)
        } ?: run {
            emitter.resume(visibleDataList)
        }
    }


    suspend fun sortData(list: ArrayList<CommonModel>) {
        fillChatsList(list)
        uidList = getUidList()
        sortVisibleData()
        sortVisibleDataByTime()
    }

    private suspend fun getUidList() = withContext(Dispatchers.IO) {
        suspendCoroutine<ArrayList<String?>> { emitter ->
            emitter.resume(chatsList.mapNotNull {
                it.uidArray?.apply { remove(auth.currentUser?.uid) }
            }.flatten().toCollection(ArrayList()))
        }
    }

    private fun fillChatsList(list: ArrayList<CommonModel>) {
        chatsList = list
    }

    private fun sortVisibleDataByTime() {
        visibleDataList.let { list ->
            list.sortBy {
                it.lastMessage?.get(currentUID)?.timestamp
            }
            list.reverse()
        }
    }

    private suspend fun sortVisibleData() =
        withContext(Dispatchers.IO) {
            suspendCoroutine<ArrayList<CommonModel>> { emitter ->
                val visibleData = ArrayList<CommonModel>()

                allUsersList.forEach {
                    uidList.forEach { uid ->
                        if (it.uid == uid) {
                            val messageItem = chatsList[uidList.indexOf(uid)]
                            visibleData.add(
                                CommonModel(
                                    username = it.username,
                                    phone = it.phone,
                                    lastMessage = messageItem.lastMessage,
                                    chatUID = messageItem.chatUID,
                                    uid = it.uid,
                                    userPicture = it.userPicture,
                                    tokens = it.tokens
                                )
                            )
                        }
                    }
                }
                emitter.resume(visibleData)
            }
        }

    suspend fun getAllUsersList() {
        withContext(Dispatchers.IO) {
            allUsersList = rtGetter.getAllUsersList()
        }
    }

    suspend fun getUserInfo() {
        withContext(Dispatchers.IO) {
            user = rtGetter.getUser(currentUID.orEmpty())
            username = user?.username

        }
    }


    fun getMessagedUsersListener() {
        var allMessagedUsersList = ArrayList<CommonModel>()
        allMessagedUsersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    allMessagedUsersList.clear()
                    snapshot.children.forEach {
                        it.getValue(CommonModel::class.java)?.let { value ->
                            if (value.permissionUidArray?.get(currentUID) == true) {
                                allMessagedUsersList.add(value)
                            }
                        }
                    }
                } else {
                    allMessagedUsersList = arrayListOf()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ERROR", error.message)
            }
        }
    }

    fun addMessagedUsersListener() {
        allMessagedUsersListener?.let {
            databaseRefMessages.addValueEventListener(it)
        }
    }

    fun removeMessagedUsersListener() {
        allMessagedUsersListener?.let {
            databaseRefMessages.removeEventListener(it)
        }
    }

    fun deleteMyChat(chatUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            rtUser.deleteChatForCurrentUser(chatUID)
        }
    }

    fun deleteChat(chatUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            rtMessage.deleteChat(chatUID)
        }
    }

}