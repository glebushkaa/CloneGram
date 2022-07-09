package com.example.clonegramtestproject.ui.activity.viewmodel

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clonegramtestproject.data.models.CodesModel
import com.example.clonegramtestproject.data.sharedPrefs.SharedPrefsHelper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream

class ActivityViewModel(
    private val sharedPrefsHelper: SharedPrefsHelper
) : ViewModel() {

    val countryCodeList: ArrayList<CodesModel> = ArrayList()

    fun getCountryCodeList(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = inputStream.bufferedReader().use { it.readText() }
            countryCodeList.addAll(
                Gson().fromJson(
                    json, Array<CodesModel>::class.java
                )
            )
        }
    }

    fun getThemeSettings(theme: Resources.Theme, sharedPreferences: SharedPreferences) {
        sharedPrefsHelper.setThemeSettings(theme, sharedPreferences)
    }

    fun getLangSettings(sharedPreferences: SharedPreferences, lang: String) =
        sharedPrefsHelper.getLanguageSettings(sharedPreferences, lang)


}