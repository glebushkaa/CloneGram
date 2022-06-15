package com.example.clonegramtestproject.ui.message.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.clonegramtestproject.MyApp
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.models.LastMessageModel
import com.example.clonegramtestproject.data.models.MessageModel
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeMessage
import com.example.clonegramtestproject.data.firebase.storage.StorageOperator
import com.example.clonegramtestproject.data.models.NotificationModel
import com.example.clonegramtestproject.data.retrofit.RetrofitHelper
import com.example.clonegramtestproject.utils.DIALOGUES_NODE
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
import retrofit2.Retrofit

class DirectMessageViewModel : ViewModel() {

    var username: String? = null
    var phone: String? = null
    var uid: String? = null
    var chatUID: String? = null
    var user: CommonModel? = null


    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(MESSAGES_NODE)

    private var messageListener: ValueEventListener? = null

    private val currentUID = FirebaseAuth.getInstance().currentUser?.uid

    val messagesLiveData = MutableLiveData<List<MessageModel>>()
    val messageLiveData = MutableLiveData<MessageModel?>()

    private val retrofitHelper = RetrofitHelper()
    private val rtMessage = RealtimeMessage()
    private val sOperator = StorageOperator()

    fun setVariables(user: CommonModel?) {
        username = user?.username
        phone = user?.phone
        uid = user?.uid
        chatUID = user?.chatUID
    }

    fun pushMessagePicture(uri: Uri) {
        user?.let {
            sOperator.pushMessagePicture(
                uri, it, username.orEmpty(), chatUID.orEmpty()
            )
        }
    }

    fun sendNotification(retrofit: Retrofit, body: String) {
        user?.let { user ->
            user.tokens?.forEach { token ->
                retrofitHelper.sendNotification(
                    retrofit,
                    token.value?.token.toString(),
                    NotificationModel(
                        username.orEmpty(),
                        body
                    )
                )
            }
        }
    }

    suspend fun setMessageListener() =
        withContext(Dispatchers.IO) {
            val messageData = ArrayList<MessageModel>()
            messageListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        messageData.clear()
                        snapshot.children.forEach { value ->
                            value.getValue(MessageModel::class.java)?.let {
                                if (it.uidPermission?.contains(uid.orEmpty()) == true) {
                                    messageData.add(it)
                                }
                            }
                        }
                        launch {
                            setLastMessage(messageData.last())
                            rtMessage.setSeenParameter(messageData, chatUID.orEmpty())
                        }
                        messagesLiveData.value = messageData
                    } else {
                        messagesLiveData.value = arrayListOf()
                        launch {
                            rtMessage.changeLastMessage(
                                chatUID.orEmpty(), LastMessageModel()
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }
        }

    private suspend fun setLastMessage(messageData: MessageModel) {
        withContext(Dispatchers.IO) {
            if (messageData.picture) {
                rtMessage.changeLastMessage(
                    chatUID.orEmpty(),
                    LastMessageModel(
                        message = null,
                        timestamp = messageData.timestamp,
                        picture = true
                    )
                )
            } else {
                rtMessage.changeLastMessage(
                    chatUID.orEmpty(),
                    LastMessageModel(
                        message = messageData.message,
                        timestamp = messageData.timestamp,
                        picture = false
                    )
                )
            }
        }
    }

    fun addMessageListener() {
        messageListener?.let {
            databaseRefMessages
                .child(chatUID.orEmpty())
                .child(DIALOGUES_NODE)
                .addValueEventListener(it)
        }
    }

    fun removeMessageListener() {
        messageListener?.let {
            databaseRefMessages
                .child(chatUID.orEmpty())
                .child(DIALOGUES_NODE)
                .removeEventListener(it)
        }
    }
}


