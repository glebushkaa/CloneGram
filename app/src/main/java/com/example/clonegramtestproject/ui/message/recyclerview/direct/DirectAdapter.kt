package com.example.clonegramtestproject.ui.message.recyclerview.direct

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.data.LastMessageData
import com.example.clonegramtestproject.data.MessageData
import com.example.clonegramtestproject.firebase.realtime.RealtimeMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class DirectAdapter(
    private val liveData: MutableLiveData<MessageData?>,
    private val user: CommonModel?,
    private val context: Context,
    private val chatUID: String
) : RecyclerView
.Adapter<DirectAdapter.MessageViewHolder>() {

    private val currentUID = FirebaseAuth.getInstance().currentUser?.uid
    private val messageList = ArrayList<MessageData>()

    private val rtMessage = RealtimeMessage()

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var message: TextView? = null
        private var timestamp: TextView? = null
        private var picture: ImageView? = null

        fun bind(userMessageData: MessageData) {


            if (itemViewType == 0 || itemViewType == 2) {
                message = itemView.findViewById(R.id.message)
                timestamp = itemView.findViewById(R.id.timestamp)

                message?.text = userMessageData.message
                timestamp?.text = SimpleDateFormat("HH:mm")
                    .format(userMessageData.timestamp)
            } else {
                picture = itemView.findViewById(R.id.picture)

                Glide.with(picture!!.context)
                    .load(userMessageData.message)
                    .into(picture!!)
            }

            setSeenOption(itemViewType, userMessageData, itemView)

            itemView.setOnLongClickListener {
                val popUp: PopupMenu =
                    if (itemViewType == 0 || itemViewType == 2) {
                        PopupMenu(
                            context, message,
                            0,
                            0,
                            R.style.CustomPopUpStyle
                        )
                    } else {
                        PopupMenu(
                            context, picture,
                            0,
                            0,
                            R.style.CustomPopUpStyle
                        )
                    }
                if (itemViewType == 0) {
                    setOutgoingMessageMenu(popUp, userMessageData)
                } else {
                    setIncomingMessageMenu(popUp, userMessageData)
                }
                popUp.show()
                true
            }
        }
    }

    override fun getItemViewType(position: Int): Int =
        messageList[position].let {
            if (it.uid == currentUID) {
                if (!it.picture) {
                    0
                } else {
                    1
                }
            } else {
                if (!it.picture) {
                    2
                } else {
                    3
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MessageViewHolder(
        when (viewType) {
            0 -> LayoutInflater.from(parent.context).inflate(
                R.layout.outgoing_message_item, parent, false
            )
            1 -> LayoutInflater.from(parent.context).inflate(
                R.layout.outgoing_picture_message_item, parent, false
            )
            2 -> LayoutInflater.from(parent.context).inflate(
                R.layout.incoming_message_item, parent, false
            )
            else -> LayoutInflater.from(parent.context).inflate(
                R.layout.incoming_picture_message_item, parent, false
            )
        }
    )


    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val userMessageInfo = messageList[position]
        holder.bind(userMessageInfo)
    }

    override fun getItemCount(): Int = messageList.size

    fun setData(newMessageList: List<MessageData>) {
        val diffUtil = DirectDiffUtil(messageList, newMessageList)
        val diffResults = DiffUtil.calculateDiff(diffUtil)
        messageList.clear()
        messageList.addAll(newMessageList)
        diffResults.dispatchUpdatesTo(this)
    }

    private fun setOutgoingMessageMenu(popUp: PopupMenu, userMessageData: MessageData) {
        popUp.inflate(R.menu.message_outgoing_menu)
        popUp.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.deleteOutgoingMessageForBoth -> {
                    deleteMessageForBoth(userMessageData)
                }
                R.id.deleteOutgoingMessageForMe -> CoroutineScope(Dispatchers.IO).launch {
                    rtMessage
                        .deleteMessageForMe(chatUID, userMessageData.messageUid.orEmpty())
                }
                R.id.editMessage -> liveData.value = userMessageData
            }
            false
        }
    }

    private fun setIncomingMessageMenu(popUp: PopupMenu, userMessageData: MessageData) {
        popUp.inflate(R.menu.message_incoming_menu)
        popUp.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.deleteIncomingMessageForBoth -> {
                    deleteMessageForBoth(userMessageData)
                }
                R.id.deleteIncomingMessageForMe -> CoroutineScope(Dispatchers.IO).launch {
                    rtMessage
                        .deleteMessageForMe(chatUID, userMessageData.messageUid.orEmpty())
                }
            }
            false
        }
    }

    private fun setSeenOption(itemViewType: Int, userMessageData: MessageData, itemView: View) {
        if (itemViewType == 0 || itemViewType == 1) {
            if (userMessageData == messageList.last()
                && userMessageData.seen != null
            ) {
                val seen: TextView = itemView.findViewById(R.id.tvSeen)
                seen.visibility = View.VISIBLE
            } else {
                val seen: TextView = itemView.findViewById(R.id.tvSeen)
                seen.visibility = View.GONE
            }
        }
    }

    private fun deleteMessageForBoth(userMessageData: MessageData) {
        CoroutineScope(Dispatchers.IO).launch {
            rtMessage
                .deleteMessage(chatUID, userMessageData.messageUid.orEmpty())
            if (messageList.last() == userMessageData) {
                rtMessage.setLastMessage(
                    arrayListOf(currentUID.orEmpty(), user?.uid.orEmpty()),
                    chatUID,
                    messageList[messageList.indexOf(userMessageData) - 1].let { messageInfo ->
                        LastMessageData(
                            message = messageInfo.message,
                            timestamp = messageInfo.timestamp,
                            picture = messageInfo.picture
                        )
                    }
                )
            }
        }

    }

}