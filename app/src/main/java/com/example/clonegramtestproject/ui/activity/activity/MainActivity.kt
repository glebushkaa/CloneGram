package com.example.clonegramtestproject.ui.activity.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.language.LanguageHelper
import com.example.clonegramtestproject.data.models.CodesModel
import com.example.clonegramtestproject.databinding.ActivityMainBinding
import com.example.clonegramtestproject.ui.activity.viewmodel.ActivityViewModel
import com.example.clonegramtestproject.utils.settingsName
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<ActivityViewModel>()
    private var binding: ActivityMainBinding? = null

    private val langHelper : LanguageHelper by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.apply {
            getThemeSettings(theme, getSharedPrefs())
            getLangSettings(getSharedPrefs(),getString(R.string.lang))?.let {
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