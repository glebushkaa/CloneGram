package com.example.clonegramtestproject

import com.example.clonegramtestproject.databinding.IconChangeItemBinding
import com.example.clonegramtestproject.databinding.ThemeChangeItemBinding
import com.example.clonegramtestproject.utils.*
import com.google.android.material.button.MaterialButton


fun ThemeChangeItemBinding.setSelectedColorTheme(theme: String) {
    val id = when (theme) {
        YELLOW_THEME -> bYellowTheme.id
        RED_THEME -> bRedTheme.id
        BLUE_THEME -> bBlueTheme.id
        PURPLE_THEME -> bPurpleTheme.id
        DARK_PURPLE_THEME -> bDarkPurpleTheme.id
        ORANGE_THEME -> bOrangeTheme.id
        else -> bGreenTheme.id
    }
    changeThemeButton(id)
}

private fun ThemeChangeItemBinding.changeThemeButton(btnId: Int) {
    val btnList = arrayListOf(
        bYellowTheme, bOrangeTheme, bPurpleTheme,
        bBlueTheme, bRedTheme, bDarkPurpleTheme, bGreenTheme
    )

    btnList.forEach {
        if (it.id != btnId) {
            it.changeStrokeWidth(0)
        } else {
            it.changeStrokeWidth(3)
        }
    }
}

fun IconChangeItemBinding.changeIconButton(btnId: Int) {
    val btnList = arrayListOf(
        fire, disabled, heart, star
    )
    btnList.forEach {
        if (it.id != btnId) {
            it.changeElevation(0f)
        } else {
            it.changeElevation(8f)
        }
    }
}

fun IconChangeItemBinding.setSelectedIcon(icon : Int){
    val id = when(icon){
        HEART -> heart.id
        FIRE -> fire.id
        STAR -> star.id
        else -> disabled.id
    }
    changeIconButton(id)
}


