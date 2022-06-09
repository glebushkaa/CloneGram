package com.example.clonegramtestproject.ui.message.recyclerview.general

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.firebase.realtime.RealtimeMessage
import com.example.clonegramtestproject.firebase.storage.StorageOperator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class GeneralAdapter(
    private val context: Context,
    private val clickListener: OnItemClickListener
) : RecyclerView.
Adapter<GeneralAdapter.GeneralViewHolder>() {

    private val firebaseChanger = RealtimeUser()
    private val firebaseOperator = RealtimeMessage()

    private val currentUID = Firebase.auth.currentUser?.uid

    private val oldUsersArrayList = ArrayList<CommonModel>()

    inner class GeneralViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameField: TextView = itemView.findViewById(R.id.tvTitle)
        private val lastMessageField: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        private val userIcon: AppCompatImageView = itemView.findViewById(R.id.userIcon)

        fun bind(
            userData: CommonModel,
            itemClickListener: OnItemClickListener
        ) {
            userData.userPicture?.let {
                Glide.with(context)
                    .load(it)
                    .circleCrop()
                    .into(userIcon)
            }
            usernameField.text = userData.username

            if (userData.lastMessage?.get(currentUID)?.picture == true) {
                lastMessageField.text = context.getString(R.string.picture)
            } else {
                lastMessageField.text = userData.lastMessage?.get(currentUID)?.message
            }

            userData.lastMessage?.get(currentUID)?.timestamp?.let {
                timestamp.text = SimpleDateFormat("HH:mm\nd/MM")
                    .format(it)
            }

            itemView.setOnClickListener {
                itemClickListener.onItemClicked(userData)
            }
            itemView.setOnLongClickListener {

                val popUp = PopupMenu(context, userIcon,
                    0,
                    0,
                    R.style.CustomPopUpStyle)

                popUp.inflate(R.menu.general_item_menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.deleteChatForMe -> CoroutineScope(Dispatchers.IO).launch{
                            firebaseChanger
                                .deleteChatForCurrentUser(userData.chatUID.orEmpty())
                        }
                        R.id.deleteChatForBoth ->
                            CoroutineScope(Dispatchers.IO).launch{
                                firebaseOperator
                                    .deleteChat(userData.chatUID.orEmpty())
                            }
                    }
                    false
                }
                popUp.show()
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(
            R.layout.item_general_message, parent, false
        )
        return GeneralViewHolder(item)
    }

    override fun onBindViewHolder(holder: GeneralViewHolder, position: Int) {
        val userMessageData = oldUsersArrayList[position]
        holder.bind(userMessageData, clickListener)
    }

    override fun getItemCount() = oldUsersArrayList.size

    fun setData(newUsersArrayList: ArrayList<CommonModel>) {
        val diffUtil = GeneralDiffUtil(oldUsersArrayList, newUsersArrayList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldUsersArrayList.clear()
        oldUsersArrayList.addAll(newUsersArrayList)
        diffResult.dispatchUpdatesTo(this)
    }

    interface OnItemClickListener {
        fun onItemClicked(user: CommonModel)
    }
}