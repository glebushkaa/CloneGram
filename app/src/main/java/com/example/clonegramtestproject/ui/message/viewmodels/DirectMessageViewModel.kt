package com.example.clonegramtestproject.ui.message.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeMessage
import com.example.clonegramtestproject.data.firebase.storage.StorageOperator
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.models.LastMessageModel
import com.example.clonegramtestproject.data.models.MessageModel
import com.example.clonegramtestproject.data.models.NotificationModel
import com.example.clonegramtestproject.data.retrofit.RetrofitHelper
import com.example.clonegramtestproject.utils.DIALOGUES_NODE
import com.example.clonegramtestproject.utils.MESSAGES_NODE
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DirectMessageViewModel(
    private val rtMessage: RealtimeMessage,
    private val sOperator: StorageOperator,
    private val retrofitHelper: RetrofitHelper,
    currentUser: FirebaseUser?,
    firebaseDatabase: FirebaseDatabase
) : ViewModel() {

    var user: CommonModel? = null
    var myUsername : String? = null

    var isEditMessage = false
    var editedMessageInfo: MessageModel? = null
    var lastUri : Uri? = null

    private var messageListener: ValueEventListener? = null

    val currentUID = currentUser?.uid
    private val refMessages = firebaseDatabase.getReference(MESSAGES_NODE)

    val messagesLiveData = MutableLiveData<List<MessageModel>>()

    fun pushMessagePicture(uri: Uri) =
        sOperator.pushMessagePicture(uri,user?.chatUID.orEmpty())

    suspend fun sendNotification(body: String) {
        user?.apply {
            tokens?.forEach { token ->
                retrofitHelper.sendNotification(
                    token.value?.token.toString(),
                    NotificationModel(
                        username.orEmpty(),
                        body
                    )
                )
            }
        }
    }

    fun deleteMessage(
        userMessageModel: MessageModel, prelastMessage: MessageModel?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            rtMessage
                .deleteMessage(user?.chatUID.orEmpty(), userMessageModel.messageUid.orEmpty())
            prelastMessage?.apply {
                changeLastMessage(prelastMessage)
            }
        }
    }

    fun deleteMessageForMe(messageUID: String, prelastMessage: MessageModel?) {
        viewModelScope.launch {
            rtMessage.deleteMessageForMe(user?.chatUID.orEmpty(), messageUID)
            prelastMessage?.apply {
                changeLastMessage(prelastMessage)
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
                                if (it.permission?.contains(currentUID.orEmpty()) == true) {
                                    messageData.add(it)
                                }
                            }
                        }
                        rtMessage.setSeenParameter(messageData, user?.chatUID.orEmpty())
                        messagesLiveData.value = messageData
                    } else {
                        messagesLiveData.value = arrayListOf()
                        launch {
                            rtMessage.changeLastMessage(
                                user?.chatUID.orEmpty(), LastMessageModel()
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }
        }

    private fun editMessage(message: String, messageUID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            rtMessage.editMessage(
                message,
                user?.chatUID.orEmpty(),
                messageUID
            )
        }
    }

    private fun sendMessage(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            rtMessage.sendMessage(
                userUID = user?.uid.orEmpty(),
                MessageModel(
                    permission = arrayListOf(
                        currentUID.orEmpty(),
                        user?.uid.orEmpty()
                    ),
                    uid = currentUID,
                    message = text,
                    timestamp = System.currentTimeMillis(),
                    picture = false
                ),
                user?.chatUID.orEmpty()
            )
        }
    }

    private fun changeLastMessage(messageModel: MessageModel) {
        viewModelScope.launch(Dispatchers.IO) {
            rtMessage.changeLastMessage(
                user?.chatUID.orEmpty(), LastMessageModel(
                    message = messageModel.message,
                    timestamp = messageModel.timestamp,
                    picture = messageModel.picture
                )
            )
        }
    }

    fun setLastMessage(text: String?, picture: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            rtMessage.setLastMessage(
                arrayListOf(currentUID.orEmpty(), user?.uid.orEmpty()),
                user?.chatUID.orEmpty(),
                LastMessageModel(
                    message = text,
                    timestamp = System.currentTimeMillis(),
                    picture = picture
                )
            )
        }
    }

    fun doSendAction(text: String) {
        if (isEditMessage) {
            editMessage(text, editedMessageInfo?.messageUid.orEmpty())
            isEditMessage = false
            editedMessageInfo = null
            setLastMessage(text, false)
        } else {
            lastUri?.let { uri ->
                pushMessagePicture(uri)
            }
           /* pushMessagePicture(uri)
            setLastMessage(null, true)
            sendNotification(getString(R.string.picture))*//*
            setLastMessage(text, false)*/
        }
    }

    fun addMessageListener() {
        messageListener?.let {
            refMessages
                .child(user?.chatUID.orEmpty())
                .child(DIALOGUES_NODE)
                .addValueEventListener(it)
        }
    }

    fun removeMessageListener() {
        messageListener?.let {
            refMessages
                .child(user?.chatUID.orEmpty())
                .child(DIALOGUES_NODE)
                .removeEventListener(it)
        }
    }
}


