package com.example.clonegramtestproject.ui.login.viewmodels

import androidx.lifecycle.ViewModel
import com.example.clonegramtestproject.data.models.CodesModel
import kotlin.collections.ArrayList

class CountryViewModel : ViewModel() {

    var chosenCountry: String? = null

    var countryList = arrayListOf<CodesModel>()
    var filteredList = arrayListOf<CodesModel>()

   suspend fun filterList(text: String?) : ArrayList<CodesModel>{
       filteredList.clear()
       countryList.forEach {
           if (it.name.lowercase().contains(text.orEmpty().lowercase())) {
              filteredList.add(it)
           }
       }
       return filteredList
    }


    fun setVariables(list: ArrayList<CodesModel>?, country: String?) {
        list?.let {
            countryList.addAll(it)
            filteredList.addAll(it)
        }
        chosenCountry = country
    }

    fun getCountryCode(position: Int): String {
        val countryCode = filteredList[position].dial_code.trim()
        chosenCountry = countryCode.substring(1, countryCode.length)
        return chosenCountry.orEmpty()
    }

}