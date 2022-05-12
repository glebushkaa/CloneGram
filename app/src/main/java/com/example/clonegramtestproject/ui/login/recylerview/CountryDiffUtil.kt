package com.example.clonegramtestproject.ui.login.recylerview

import androidx.recyclerview.widget.DiffUtil
import com.example.clonegramtestproject.data.CountriesCodes

class CountryDiffUtil(
    private val oldList : ArrayList<CountriesCodes>,
    private val newList : ArrayList<CountriesCodes>
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