package com.example.clonegramtestproject.ui.message.recyclerview.general

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.databinding.ItemGeneralMessageBinding
import java.text.SimpleDateFormat

class GeneralAdapter(private val uid : String) : RecyclerView.Adapter<GeneralAdapter.GeneralViewHolder>() {

    private var rvListener : OnItemClickListener? = null

    private var oldUsersArrayList = ArrayList<CommonModel>()

    private lateinit var binding: ItemGeneralMessageBinding

    inner class GeneralViewHolder(val binding: ItemGeneralMessageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: CommonModel) {
            binding.apply {
                user.apply {
                    userPicture?.let {
                        Glide.with(userIcon.context).load(it).circleCrop().into(userIcon)
                    }
                    tvUsername.text = username
                    if (lastMessage?.get(uid)?.picture == true) {
                        tvMessage.text = userIcon.context.getString(R.string.picture)
                    } else {
                        tvMessage.text = lastMessage?.get(uid)?.message
                    }

                    lastMessage?.get(uid)?.timestamp?.let {
                        timestamp.text = SimpleDateFormat("HH:mm\nd/MM")
                            .format(it)
                    }
                    setClickListeners(this,binding)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralViewHolder {
        binding = ItemGeneralMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GeneralViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GeneralViewHolder, position: Int) {
        val userMessageData = oldUsersArrayList[position]
        holder.bind(userMessageData)
    }

    override fun getItemCount() = oldUsersArrayList.size

    fun setData(newUsersArrayList: ArrayList<CommonModel>) {
        val diffUtil = GeneralDiffUtil(oldUsersArrayList, newUsersArrayList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldUsersArrayList.clear()
        oldUsersArrayList.addAll(newUsersArrayList)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun setClickListeners(user : CommonModel, binding: ItemGeneralMessageBinding){
        binding.apply {

            binding.root.setOnClickListener {
                rvListener?.onItemClicked(user)
            }

            binding.root.setOnLongClickListener {
                val popUp = PopupMenu(
                    userIcon.context, userIcon,
                    0, 0,
                    R.style.CustomPopUpStyle
                )
                popUp.inflate(R.menu.general_item_menu)
                popUp.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.deleteChatForMe -> rvListener?.deleteMyChatListener(user.chatUID.orEmpty())
                        R.id.deleteChatForBoth -> rvListener?.deleteChatListener(user.chatUID.orEmpty())
                    }
                    false
                }
                popUp.show()
                true
            }
        }
    }

    fun setOnItemClickedListener(listener: OnItemClickListener){
        rvListener = listener
    }

    interface OnItemClickListener {
        fun onItemClicked(user: CommonModel)

        fun deleteMyChatListener(chatUID : String)

        fun deleteChatListener(chatUID : String)
    }
}