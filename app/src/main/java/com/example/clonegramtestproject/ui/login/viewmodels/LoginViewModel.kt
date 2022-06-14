package com.example.clonegramtestproject.ui.login.viewmodels

import androidx.lifecycle.ViewModel
import com.example.clonegramtestproject.FlagsEmojies
import com.example.clonegramtestproject.data.CountriesCodes
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LoginViewModel : ViewModel() {

    var countryCodeArrayList = arrayListOf<CountriesCodes>()

    var code: String? = null
    var chosenCountry: String? = null
    var phoneNumber: String? = null

    fun fillCountryCodesList(list: ArrayList<CountriesCodes>) {
        countryCodeArrayList = list
    }

    fun setCountry() : String?{
        countryCodeArrayList.forEach {
            if(code == it.dial_code){
                chosenCountry = it.name
                return  "${FlagsEmojies.countriesFlags[it.code]}  $chosenCountry"
            }
        }
        return null
    }

}