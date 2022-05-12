package com.example.clonegramtestproject.ui.message.recyclerview.general

import androidx.recyclerview.widget.DiffUtil
import com.example.clonegramtestproject.data.CommonModel

class GeneralDiffUtil(
    private val oldArrayList: ArrayList<CommonModel>,
    private val newArrayList: ArrayList<CommonModel>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldArrayList.size


    override fun getNewListSize() = newArrayList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldArrayList[oldItemPosition].userPicture == newArrayList[newItemPosition].userPicture

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldArrayList[oldItemPosition] == newArrayList[newItemPosition]
}