package com.example.clonegramtestproject.ui.message.recyclerview.direct

import androidx.recyclerview.widget.DiffUtil
import com.example.clonegramtestproject.data.MessageData

class DirectDiffUtil(
    private val oldArrayList : List<MessageData>,
    private val newArrayList : List<MessageData>
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