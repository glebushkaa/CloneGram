package com.example.clonegramtestproject

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.clonegramtestproject.data.CountriesCodes
import com.example.clonegramtestproject.databinding.ActivityMainBinding
import com.example.clonegramtestproject.utils.languagePreferencesName
import com.example.clonegramtestproject.utils.settingsName
import com.example.clonegramtestproject.utils.themePreferencesName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.InputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    val countryCodeArrayList = arrayListOf<CountriesCodes>()

    override fun onCreate(savedInstanceState: Bundle?) {
        getInfoFromSharedPrefs()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setArrayOfFlags()
    }

    private fun setArrayOfFlags() {
        val inputStream: InputStream = assets.open(
            "CountryCodes_${getString(R.string.lang)}.json")
        lifecycleScope.launch(Dispatchers.IO) {

            val json = inputStream.bufferedReader().use { it.readText() }
            val jsonArr = JSONArray(json)

            for (i in 0 until jsonArr.length()) {
                val jsonObj = jsonArr.getJSONObject(i)
                val country = jsonObj.getString("name")
                val dialCode = jsonObj.getString("dial_code")
                val code = jsonObj.getString("code")

                countryCodeArrayList.add(CountriesCodes(country, dialCode, code))
            }
        }
    }

    private fun getInfoFromSharedPrefs() {
        val sharedPreferences = getSharedPreferences(settingsName, Context.MODE_PRIVATE)

        if (sharedPreferences.contains(themePreferencesName)) {
            when (sharedPreferences.getString(themePreferencesName, "")) {
                "yellow" -> theme.applyStyle(R.style.Theme_YellowClonegram, true)
                "red" -> theme.applyStyle(R.style.Theme_RedClonegram, true)
                "green" -> theme.applyStyle(R.style.Theme_GreenClonegram, true)
                "purple" -> theme.applyStyle(R.style.Theme_PurpleClonegram, true)
                "blue" -> theme.applyStyle(R.style.Theme_BlueClonegram, true)
            }
        }else{
            sharedPreferences.edit()?.putString(
                themePreferencesName, "yellow"
            )?.apply()
            theme.applyStyle(R.style.Theme_YellowClonegram, true)
        }

        if (sharedPreferences.contains(languagePreferencesName)) {
            setLanguage(sharedPreferences.getString(languagePreferencesName, "").orEmpty())
        }else{
            sharedPreferences.edit()?.putString(
                languagePreferencesName, getString(R.string.lang)
            )?.apply()
        }
    }

    private fun setLanguage(lang: String) {
        Locale.setDefault(Locale(lang))
        val config = Configuration()
        config.locale = Locale(lang)
        baseContext.resources.updateConfiguration(
            config,
            baseContext.resources.displayMetrics
        )
    }


}