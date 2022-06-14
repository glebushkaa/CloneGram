package com.example.clonegramtestproject

import android.content.Context
import android.content.res.Configuration
import java.util.*

class LanguageHelper {

    fun setLanguage(lang: String,baseContext : Context) {
        Locale.setDefault(Locale(lang))
        val config = Configuration()
        config.locale = Locale(lang)
        baseContext.resources.updateConfiguration(
            config,
            baseContext.resources.displayMetrics
        )
    }

}