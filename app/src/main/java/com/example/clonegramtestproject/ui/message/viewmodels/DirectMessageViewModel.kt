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
import com.google.firebase.auth.FirebaseAuth
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
    firebaseDatabase: FirebaseDatabase
) : ViewModel() {

    companion object {
        private const val PICTURE_NOTIFICATION_TEXT = "*picture*"
    }

    var user: CommonModel? = null
    var myUsername: String? = null

    var isEditMessage = false
    var editedMessageInfo: MessageModel? = null
    var isEditedMessageLast = false

    private var messageListener: ValueEventListener? = null

    private val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUID = currentUser?.uid
    private val refMessages = firebaseDatabase.getReference(MESSAGES_NODE)

    val messagesLiveData = MutableLiveData<List<MessageModel>>()

    private suspend fun pushMessagePicture(uri: Uri) =
        sOperator.pushMessagePicture(uri, user?.chatUID.orEmpty())


    private suspend fun sendNotification(body: String) {
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

    private fun sendMessage(text: String, picture: Boolean) {
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
                    picture = picture
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

    private fun setLastMessage(text: String?, picture: Boolean) {
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

    fun doSendAction(
        text: String? = null,
        uri: Uri? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isEditMessage) {
                editMessage(text.orEmpty(), editedMessageInfo?.messageUid.orEmpty())
                if (isEditedMessageLast) {
                    setLastMessage(text, false)
                }
                isEditedMessageLast = false
                isEditMessage = false
                editedMessageInfo = null

            } else {
                uri?.let {
                    sendMessage(pushMessagePicture(uri), true)
                    sendNotification(PICTURE_NOTIFICATION_TEXT)
                } ?: run {
                    sendMessage(text.orEmpty(), false)
                    sendNotification(text.orEmpty())
                }
                setLastMessage(text, uri?.equals(null) == false)
            }
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


