package com.example.clonegramtestproject.data.sharedPrefs

import android.content.SharedPreferences
import android.content.res.Resources
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.utils.languagePreferencesName
import com.example.clonegramtestproject.utils.themePreferencesName

class SharedPrefsHelper {

    fun getThemeSettings(theme: Resources.Theme, sharedPreferences: SharedPreferences) {
        if (sharedPreferences.contains(themePreferencesName)) {
            when (sharedPreferences.getString(themePreferencesName, "")) {
                "yellow" -> theme.applyStyle(R.style.Theme_YellowClonegram, true)
                "red" -> theme.applyStyle(R.style.Theme_RedClonegram, true)
                "green" -> theme.applyStyle(R.style.Theme_GreenClonegram, true)
                "purple" -> theme.applyStyle(R.style.Theme_PurpleClonegram, true)
                "blue" -> theme.applyStyle(R.style.Theme_BlueClonegram, true)
            }
        } else {
            sharedPreferences.edit()?.putString(
                themePreferencesName, "yellow"
            )?.apply()
            theme.applyStyle(R.style.Theme_YellowClonegram, true)
        }
    }

    fun getLanguageSettings(sharedPreferences: SharedPreferences): String? {
        return if (sharedPreferences.contains(languagePreferencesName)) {
            sharedPreferences.getString(
                languagePreferencesName,
                ""
            ).orEmpty()
        } else {
            sharedPreferences.edit()?.putString(
                languagePreferencesName, Resources.getSystem().getString(R.string.lang)
            )?.apply()
            null
        }
    }

    fun getLanguage(sharedPreferences: SharedPreferences): String =
        sharedPreferences.getString(languagePreferencesName, "").orEmpty()

}