package com.example.clonegramtestproject.ui.login.recylerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
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

        fun bind() {
            oldArrayList[bindingAdapterPosition].apply {
                val flag = countryFlags[code]
                country.text = "$flag $name"
                countryCode.text = dial_code
            }
        }

        init {
            itemView.setOnClickListener {
                clickListener(bindingAdapterPosition)
            }
        }
    }

}