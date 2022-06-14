package com.example.clonegramtestproject.ui.message.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.models.LastMessageModel
import com.example.clonegramtestproject.data.models.MessageModel
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeMessage
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

class DirectMessageViewModel : ViewModel() {

    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(MESSAGES_NODE)

    private var messageListener: ValueEventListener? = null

    private val currentUID = FirebaseAuth.getInstance().currentUser?.uid

    val messagesLiveData = MutableLiveData<List<MessageModel>>()
    val messageLiveData = MutableLiveData<MessageModel?>()

    private val rtMessage = RealtimeMessage()

    suspend fun setMessageListener(chatUID: String, user : CommonModel) =
        withContext(Dispatchers.IO){
        val messageData = ArrayList<MessageModel>()
        messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    messageData.clear()
                    for (message in snapshot.children) {
                        message.getValue(
                            MessageModel::class.java
                        )?.let {
                            if (it.uidPermission?.contains(currentUID.orEmpty()) == true) {
                                messageData.add(
                                    it
                                )
                            }
                        }
                    }
                    if (messageData.last().picture) {
                        launch {
                            rtMessage.changeLastMessage(
                                chatUID,
                                LastMessageModel(
                                    message = null,
                                    timestamp = messageData.last().timestamp,
                                    picture = true
                                )
                            )
                        }
                    } else {
                       launch {
                           rtMessage.changeLastMessage(
                               chatUID,
                               LastMessageModel(
                                   message = messageData.last().message,
                                   timestamp = messageData.last().timestamp,
                                   picture = false
                               )
                           )
                       }
                    }
                    launch {
                        rtMessage.setSeenParameter(messageData, chatUID)
                    }
                    messagesLiveData.value = messageData
                } else {
                    messagesLiveData.value = arrayListOf()

                    launch {
                        rtMessage.changeLastMessage(
                            chatUID, LastMessageModel()
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }.let {
            databaseRefMessages
                .child(chatUID)
                .child(DIALOGUES_NODE)
                .addValueEventListener(it)
        }
    }



    fun removeMessageListener(chatUID: String) {
        messageListener?.let {
            databaseRefMessages
                .child(chatUID)
                .child(DIALOGUES_NODE)
                .removeEventListener(it)
        }
    }
}


