package com.example.clonegramtestproject.ui.message.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.clonegramtestproject.data.MessageData
import com.example.clonegramtestproject.utils.MESSAGES_NODE
import com.example.clonegramtestproject.utils.SEEN_NODE
import com.example.clonegramtestproject.utils.USERS_MESSAGES_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DirectMessageViewModel : ViewModel() {

    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(USERS_MESSAGES_NODE)

    private var messageListener: ValueEventListener? = null

    private val currentUID = FirebaseAuth.getInstance().currentUser?.uid

    val messagesLiveData = MutableLiveData<List<MessageData>>()
    val messageLiveData = MutableLiveData<MessageData?>()


    fun setMessageListener(chatUID: String) {

        val messageData = ArrayList<MessageData>()
        messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    messageData.clear()
                    for (message in snapshot.children) {
                        message.getValue(
                            MessageData::class.java
                        )?.let {
                            if (it.uidPermission?.contains(currentUID.orEmpty()) == true) {
                                messageData.add(
                                    it
                                )
                            }
                        }
                    }
                    setSeenParameter(messageData, chatUID)
                    messagesLiveData.value = messageData
                } else {
                    messagesLiveData.value = arrayListOf()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }.let {
            databaseRefMessages
                .child(chatUID)
                .child(MESSAGES_NODE)
                .addValueEventListener(it)
        }
    }

    private fun setSeenParameter(messageData: ArrayList<MessageData>, chatUID: String) {
        if (messageData.isNotEmpty()) {
            messageData[messageData.size - 1].let {
                if (it.uid != currentUID) {
                    databaseRefMessages
                        .child(chatUID)
                        .child(MESSAGES_NODE)
                        .child(it.messageUid.orEmpty())
                        .child(SEEN_NODE)
                        .setValue(arrayListOf(currentUID))
                }
            }
        }
    }

    fun removeMessageListener(uid: String) {
        messageListener?.let {
            databaseRefMessages
                .child(uid)
                .removeEventListener(it)
        }
    }


}