package com.example.clonegramtestproject.ui.login.viewmodels

import androidx.lifecycle.ViewModel
import com.example.clonegramtestproject.data.CountriesCodes

class CountryViewModel : ViewModel(){

    var chosenCountry : String? = null

    var countryArrayList = arrayListOf<CountriesCodes>()
    var filteredArrayList = arrayListOf<CountriesCodes>()

    fun fillFilterList(text : String?){
        filteredArrayList.clear()

        countryArrayList.forEach {
            if (it.name.lowercase().contains(text.orEmpty().lowercase())) {
                filteredArrayList.add(it)
            }
        }
    }

}