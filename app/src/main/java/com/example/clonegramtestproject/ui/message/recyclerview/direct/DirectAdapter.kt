package com.example.clonegramtestproject.ui.message.recyclerview.direct

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.models.MessageModel
import com.example.clonegramtestproject.databinding.IncomingMessageItemBinding
import com.example.clonegramtestproject.databinding.IncomingPictureMessageItemBinding
import com.example.clonegramtestproject.databinding.OutgoingMessageItemBinding
import com.example.clonegramtestproject.databinding.OutgoingPictureMessageItemBinding
import java.text.SimpleDateFormat

class DirectAdapter(private val currentUID: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val messageList = ArrayList<MessageModel>()

    private var rvListener: OnItemClickListener? = null

    companion object {
        const val OUTGOING_MESSAGE = 0
        const val OUTGOING_PICTURE = 1
        const val INCOMING_MESSAGE = 2
        const val INCOMING_PICTURE = 3
    }


    override fun getItemViewType(position: Int): Int =
        messageList[position].let {
            if (it.uid == currentUID) {
                if (!it.picture) {
                    OUTGOING_MESSAGE
                } else {
                    OUTGOING_PICTURE
                }
            } else {
                if (!it.picture) {
                    INCOMING_MESSAGE
                } else {
                    INCOMING_PICTURE
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            INCOMING_MESSAGE -> IncomingMessageViewHolder(
                IncomingMessageItemBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
            INCOMING_PICTURE -> IncomingPictureViewHolder(
                IncomingPictureMessageItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            OUTGOING_MESSAGE -> OutgoingMessageViewHolder(
                OutgoingMessageItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> OutgoingPictureViewHolder(
                OutgoingPictureMessageItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            INCOMING_MESSAGE -> (holder as IncomingMessageViewHolder).bind()
            INCOMING_PICTURE -> (holder as IncomingPictureViewHolder).bind()
            OUTGOING_MESSAGE -> (holder as OutgoingMessageViewHolder).bind()
            OUTGOING_PICTURE -> (holder as OutgoingPictureViewHolder).bind()
        }
    }

    override fun getItemCount(): Int = messageList.size

    fun setData(newMessageList: List<MessageModel>) {
        val diffUtil = DirectDiffUtil(messageList, newMessageList)
        val diffResults = DiffUtil.calculateDiff(diffUtil)
        messageList.clear()
        messageList.addAll(newMessageList)
        diffResults.dispatchUpdatesTo(this)
    }

    private fun setOutgoingMessageMenu(popUp: PopupMenu, userMessageModel: MessageModel) {
        popUp.inflate(R.menu.message_outgoing_menu)
        popUp.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.deleteOutgoingMessageForBoth -> rvListener?.deleteMessageListener(
                    userMessageModel,
                    messageList
                )
                R.id.deleteOutgoingMessageForMe -> rvListener?.deleteMessageForMeListener(
                    userMessageModel.messageUid.orEmpty()
                )
                R.id.editMessage -> rvListener?.editMessage(userMessageModel)
            }
            false
        }
    }

    private fun setIncomingMessageMenu(popUp: PopupMenu, userMessageModel: MessageModel) {
        popUp.inflate(R.menu.message_incoming_menu)
        popUp.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.deleteIncomingMessageForBoth -> rvListener?.deleteMessageListener(
                    userMessageModel,
                    messageList
                )
                R.id.deleteIncomingMessageForMe -> rvListener?.deleteMessageForMeListener(
                    userMessageModel.messageUid.orEmpty()
                )
            }
            false
        }
    }

    fun setOnItemClickedListener(listener: OnItemClickListener) {
        rvListener = listener
    }

    private fun setSeenOption(userMessageModel: MessageModel, itemView: View) {

        if (userMessageModel == messageList.last()
            && userMessageModel.seen != null
        ) {
            val seen: TextView = itemView.findViewById(R.id.tvSeen)
            seen.visibility = View.VISIBLE
        } else {
            val seen: TextView = itemView.findViewById(R.id.tvSeen)
            seen.visibility = View.GONE
        }

    }

    inner class OutgoingMessageViewHolder(val binding: OutgoingMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.message.text = messageList[adapterPosition].message
            binding.timestamp.text = SimpleDateFormat("HH:mm")
                .format(messageList[adapterPosition].timestamp)
            setSeenOption(messageList[adapterPosition], itemView)
            itemView.setOnLongClickListener {
                val popUp = PopupMenu(
                    binding.message.context, binding.message,
                    0,
                    0,
                    R.style.CustomPopUpStyle
                )
                setOutgoingMessageMenu(popUp, messageList[adapterPosition])
                popUp.show()
                true
            }
        }
    }

    inner class OutgoingPictureViewHolder(val binding: OutgoingPictureMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            Glide.with(binding.picture.context).load(messageList[adapterPosition].message)
                .into(binding.picture)
            setSeenOption(messageList[adapterPosition], itemView)
            itemView.setOnLongClickListener {
                val popUp = PopupMenu(
                    binding.picture.context, binding.picture,
                    0,
                    0,
                    R.style.CustomPopUpStyle
                )
                setIncomingMessageMenu(popUp, messageList[adapterPosition])
                popUp.show()
                true
            }
        }
    }

    inner class IncomingMessageViewHolder(val binding: IncomingMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.message.text = messageList[adapterPosition].message
            binding.timestamp.text = SimpleDateFormat("HH:mm")
                .format(messageList[adapterPosition].timestamp)
            itemView.setOnLongClickListener {
                val popUp = PopupMenu(
                    binding.message.context, binding.message,
                    0,
                    0,
                    R.style.CustomPopUpStyle
                )
                setIncomingMessageMenu(popUp, messageList[adapterPosition])
                popUp.show()
                true
            }
        }
    }

    inner class IncomingPictureViewHolder(val binding: IncomingPictureMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            Glide.with(binding.picture.context).load(messageList[adapterPosition].message)
                .into(binding.picture)
            itemView.setOnLongClickListener {
                val popUp = PopupMenu(
                    binding.picture.context, binding.picture,
                    0,
                    0,
                    R.style.CustomPopUpStyle
                )
                setIncomingMessageMenu(popUp, messageList[adapterPosition])
                popUp.show()
                true
            }
        }
    }

    interface OnItemClickListener {

        fun deleteMessageListener(
            userMessageModel: MessageModel,
            messageList: ArrayList<MessageModel>
        )

        fun deleteMessageForMeListener(messageUID: String)

        fun editMessage(message : MessageModel)

    }


}