package com.example.clonegramtestproject.ui.message.recyclerview.newuser

import androidx.recyclerview.widget.DiffUtil
import com.example.clonegramtestproject.data.models.CommonModel

class NewUserDiffUtil(
    private val oldList : ArrayList<CommonModel>,
    private val newList : ArrayList<CommonModel>
    ) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].phone == newList[newItemPosition].phone
}
}