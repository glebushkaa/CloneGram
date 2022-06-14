package com.example.clonegramtestproject.ui.message.recyclerview.direct

import androidx.recyclerview.widget.DiffUtil
import com.example.clonegramtestproject.data.models.MessageModel

class DirectDiffUtil(
    private val oldArrayList : List<MessageModel>,
    private val newArrayList : List<MessageModel>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldArrayList.size
    }

    override fun getNewListSize(): Int {
        return newArrayList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldArrayList[oldItemPosition] == newArrayList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldArrayList[oldItemPosition].seen == newArrayList[newItemPosition].seen
    }
}