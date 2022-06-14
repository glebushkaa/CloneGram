package com.example.clonegramtestproject.ui.login.recylerview

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.models.CodesModel
import com.example.clonegramtestproject.databinding.CountryItemBinding

class CountryAdapter(
    private val countryFlags: Map<String, String>,
    private val clickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {

    private var oldArrayList = ArrayList<CodesModel>()
    private lateinit var binding: CountryItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        binding = CountryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = oldArrayList.size

    fun refreshData(newArrayList: ArrayList<CodesModel>) {
        val diffUtil = CountryDiffUtil(oldArrayList, newArrayList)
        val diffResults = DiffUtil.calculateDiff(diffUtil)
        oldArrayList.clear()
        oldArrayList.addAll(newArrayList)
        diffResults.dispatchUpdatesTo(this)
    }

    inner class CountryViewHolder(binding: CountryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val country = binding.tvCountryName
        private val countryCode = binding.tvCountryCode

        fun bind(){
            val flag = countryFlags[oldArrayList[adapterPosition].code]
            country.text = "$flag ${oldArrayList[adapterPosition].name}"
            countryCode.text = oldArrayList[adapterPosition].dial_code
        }

        init {
            itemView.setOnClickListener {
                clickListener(adapterPosition)
            }
        }
    }

}