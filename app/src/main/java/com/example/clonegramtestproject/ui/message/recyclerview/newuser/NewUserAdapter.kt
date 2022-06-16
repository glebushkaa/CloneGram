package com.example.clonegramtestproject.ui.message.recyclerview.newuser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.databinding.HeaderBinding
import com.example.clonegramtestproject.databinding.NewUserItemBinding

class NewUserAdapter(private val clickListener: (user: CommonModel) -> Unit) :
    RecyclerView.Adapter<NewUserAdapter.UsersViewHolder>() {

    private val oldUsersList = ArrayList<CommonModel>()

    inner class UsersViewHolder(val binding: NewUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.apply {
                tvUsername.text = oldUsersList[adapterPosition].username
                tvPhone.text = oldUsersList[adapterPosition].phone

                oldUsersList[adapterPosition].userPicture?.let {
                    Glide.with(userIcon.context).load(it).circleCrop().into(userIcon)
                }

                itemView.setOnClickListener {
                    clickListener(oldUsersList[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsersViewHolder {
        val binding = NewUserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return UsersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = oldUsersList.size

    fun setData(newUsersList: ArrayList<CommonModel>) {
        val diffUtil = NewUserDiffUtil(oldUsersList, newUsersList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldUsersList.clear()
        oldUsersList.addAll(newUsersList)
        diffResult.dispatchUpdatesTo(this)
    }
}

