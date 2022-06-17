package com.example.clonegramtestproject.data.sharedPrefs

import android.content.SharedPreferences
import android.content.res.Resources
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.utils.*

class SharedPrefsHelper {

    fun setThemeSettings(theme: Resources.Theme, sharedPreferences: SharedPreferences) {
        if (sharedPreferences.contains(themePreferencesName)) {
            when (sharedPreferences.getString(themePreferencesName, "")) {
                YELLOW_THEME -> theme.applyStyle(R.style.Theme_YellowClonegram, true)
                RED_THEME -> theme.applyStyle(R.style.Theme_RedClonegram, true)
                GREEN_THEME -> theme.applyStyle(R.style.Theme_GreenClonegram, true)
                PURPLE_THEME -> theme.applyStyle(R.style.Theme_PurpleClonegram, true)
                BLUE_THEME -> theme.applyStyle(R.style.Theme_BlueClonegram, true)
            }
        } else {
            sharedPreferences.edit()?.putString(
                themePreferencesName, YELLOW_THEME
            )?.apply()
            theme.applyStyle(R.style.Theme_YellowClonegram, true)
        }
    }

    fun getThemeSettings(sharedPreferences: SharedPreferences) : String?{
        if (sharedPreferences.contains(themePreferencesName)) {
            return sharedPreferences.getString(themePreferencesName, "")
        }
        return null
    }

    fun getLanguageSettings(sharedPreferences: SharedPreferences,lang : String): String? {
        return if (sharedPreferences.contains(languagePreferencesName)) {
            sharedPreferences.getString(
                languagePreferencesName,
                ""
            ).orEmpty()
        } else {
            sharedPreferences.edit()?.putString(
                languagePreferencesName, lang
            )?.apply()
            null
        }
    }

    fun getLanguage(sharedPreferences: SharedPreferences): String =
        sharedPreferences.getString(languagePreferencesName, "").orEmpty()

}