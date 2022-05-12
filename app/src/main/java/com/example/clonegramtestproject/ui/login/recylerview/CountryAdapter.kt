package com.example.clonegramtestproject.ui.login.recylerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CountriesCodes

class CountryAdapter(
    private val countryFlags: Map<String, String>
) :
    RecyclerView.Adapter<CountryAdapter.CountryViewHolder>(){
    private lateinit var rvListener: OnItemClickListener

    private val oldArrayList = ArrayList<CountriesCodes>()

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        rvListener = listener
    }

    class CountryViewHolder(itemView: View, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val country = itemView.findViewById<TextView>(R.id.tvCountryName)!!
        val countryCode = itemView.findViewById<TextView>(R.id.tvCountryCode)!!

        init {
            itemView.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.country_item, parent, false)
        return CountryViewHolder(itemView, rvListener)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val flag = countryFlags[oldArrayList[position].code]
        holder.country.text = flag + "  " + oldArrayList[position].name
        holder.countryCode.text = oldArrayList[position].dial_code
    }

    override fun getItemCount() = oldArrayList.size

    fun refreshData(newArrayList : ArrayList<CountriesCodes>){
        val diffUtil = CountryDiffUtil(oldArrayList,newArrayList)
        val diffResults = DiffUtil.calculateDiff(diffUtil)
        oldArrayList.clear()
        oldArrayList.addAll(newArrayList)
        diffResults.dispatchUpdatesTo(this)
    }

}