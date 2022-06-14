package com.example.clonegramtestproject.ui.login.recylerview

import androidx.recyclerview.widget.DiffUtil
import com.example.clonegramtestproject.data.models.CodesModel

class CountryDiffUtil(
    private val oldList : ArrayList<CodesModel>,
    private val newList : ArrayList<CodesModel>
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
        return oldList[oldItemPosition].name == newList[newItemPosition].name
    }
}