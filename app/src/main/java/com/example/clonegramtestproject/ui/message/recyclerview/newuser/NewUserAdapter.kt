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

class NewUserAdapter(private val itemClickListener: OnItemClickListener) : RecyclerView
.Adapter<NewUserAdapter.UsersViewHolder>() {

    private val oldUsersList = ArrayList<CommonModel>()

    inner class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val username: TextView = itemView.findViewById(R.id.tvUsername)
        private val phone: TextView = itemView.findViewById(R.id.tvPhone)
        private val userIcon : ImageView = itemView.findViewById(R.id.userIcon)

        fun bind(user: CommonModel, clickListener: OnItemClickListener) {
            username.text = user.username
            phone.text = user.phone

            user.userPicture?.let{
                Glide.with(userIcon.context)
                    .load(it)
                    .circleCrop()
                    .into(userIcon)
            }

            itemView.setOnClickListener {
                clickListener.onItemClicked(user)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsersViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.new_user_item, parent, false
        )

        return UsersViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = oldUsersList[position]
        holder.bind(user, itemClickListener)
    }

    override fun getItemCount() = oldUsersList.size

    fun setData(newUsersList: ArrayList<CommonModel>) {
        val diffUtil = NewUserDiffUtil(oldUsersList, newUsersList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldUsersList.clear()
        oldUsersList.addAll(newUsersList)
        diffResult.dispatchUpdatesTo(this)
    }

    interface OnItemClickListener {
        fun onItemClicked(user: CommonModel)
    }


}

