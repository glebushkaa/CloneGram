package com.example.clonegramtestproject.ui.activity.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.clonegramtestproject.LanguageHelper
import com.example.clonegramtestproject.ui.activity.viewmodel.ActivityViewModel
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.sharedPrefs.SharedPrefsHelper
import com.example.clonegramtestproject.data.models.CodesModel
import com.example.clonegramtestproject.databinding.ActivityMainBinding
import com.example.clonegramtestproject.utils.settingsName
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<ActivityViewModel>()
    private var binding: ActivityMainBinding? = null

    private val langHelper = LanguageHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.apply {
            getThemeSettings(theme, getSharedPrefs())
            getLangSettings(getSharedPrefs())?.let {
                langHelper.setLanguage(it, baseContext)
            }
            getCountryCodeList(getInputStream())
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }

    fun getCountryCodeList(): ArrayList<CodesModel> = viewModel.countryCodeList

    private fun getInputStream(): InputStream =
        assets.open("CountryCodes_${getString(R.string.lang)}.json")


    private fun getSharedPrefs() = getSharedPreferences(settingsName, Context.MODE_PRIVATE)
}