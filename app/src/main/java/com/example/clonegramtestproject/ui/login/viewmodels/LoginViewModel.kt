package com.example.clonegramtestproject.ui.login.viewmodels

import androidx.lifecycle.ViewModel
import com.example.clonegramtestproject.data.Flags
import com.example.clonegramtestproject.data.models.CodesModel

class LoginViewModel : ViewModel() {

    var countryCodeArrayList = arrayListOf<CodesModel>()

    var code: String? = null
    var chosenCountry: String? = null
    var phoneNumber: String? = null

    fun fillCountryCodesList(list: ArrayList<CodesModel>) {
        countryCodeArrayList = list
    }

    fun setCountry() : String?{
        countryCodeArrayList.forEach {
            if(code == it.dial_code){
                chosenCountry = it.name
                return  "${Flags.countriesFlags[it.code]}  $chosenCountry"
            }
        }
        return null
    }

}