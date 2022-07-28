package com.example.clonegramtestproject.data.sharedPrefs

import android.content.SharedPreferences
import android.content.res.Resources
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.utils.*

class PrefsHelper(private val prefs : SharedPreferences){

    fun setThemeSettings(theme : Resources.Theme) {
        theme.apply {
            if (prefs.contains(themePreferencesName)) {
                when (prefs.getString(themePreferencesName, "")) {
                    YELLOW_THEME -> applyStyle(R.style.Theme_YellowClonegram, true)
                    RED_THEME -> applyStyle(R.style.Theme_RedClonegram, true)
                    DARK_PURPLE_THEME -> applyStyle(R.style.Theme_DarkPurpleClonegram, true)
                    PURPLE_THEME -> applyStyle(R.style.Theme_PurpleClonegram, true)
                    BLUE_THEME -> applyStyle(R.style.Theme_BlueClonegram, true)
                    ORANGE_THEME -> applyStyle(R.style.Theme_OrangeClonegram, true)
                    GREEN_THEME -> applyStyle(R.style.Theme_GreenClonegram, true)
                }
            } else {
                prefs.edit()?.putString(
                    themePreferencesName, YELLOW_THEME
                )?.apply()
                applyStyle(R.style.Theme_YellowClonegram, true)
            }
        }
    }

    fun getThemeSettings(): String? {
        if (prefs.contains(themePreferencesName)) {
            return prefs.getString(themePreferencesName, "")
        }
        return null
    }

    fun getLanguageSettings(lang: String): String? {
        return if (prefs.contains(languagePreferencesName)) {
            prefs.getString(
                languagePreferencesName,
                ""
            ).orEmpty()
        } else {
            prefs.edit()?.putString(
                languagePreferencesName, lang
            )?.apply()
            null
        }
    }

    fun editLang(lang: String){
        prefs.edit()?.putString(
            languagePreferencesName, lang
        )?.apply()
    }

    fun editTheme(color : String){
        prefs.edit()?.putString(
            themePreferencesName, color
        )?.apply()
    }

}