package com.example.clonegramtestproject.utils

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import com.example.clonegramtestproject.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun View.showSnackbar(text: String, textColor: Int? = null, backgroundTint: Int? = null) {
    val snackbar = Snackbar.make(this, text, Snackbar.LENGTH_LONG)
    textColor?.let {
        snackbar.setTextColor(it)
    }
    backgroundTint?.let {
        snackbar.setBackgroundTint(it)
    }
    snackbar.show()
}

fun Activity.showSoftKeyboard(view: View) {
    view.requestFocus()
    val imm: InputMethodManager =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.closeSoftKeyboard(view: View) {
    val imm: InputMethodManager =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Window.getSoftInputMode(): Int {
    return attributes.softInputMode
}

fun Activity.changeStatusBarColor(color: Int) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color
}

fun MaterialButton.changeStrokeWidth(width: Int) {
    strokeWidth = width
}

fun Context.getColorAppColor(): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(
        R.attr.appColor, typedValue, true
    )
    return typedValue.data
}

fun MaterialButton.changeElevation(elevation: Float) {
    this.elevation = elevation
}

fun RadioGroup.setCheckedLangButton(lang: String) {
    when (lang) {
        UKR -> check(R.id.ukrainianBtn)
        RU -> check(R.id.russianBtn)
        EN -> check(R.id.englishBtn)
    }
}

fun ImageView.setPremiumIcon(icon: Int) {
    val res = when (icon) {
        FIRE -> R.drawable.ic_premium_fire
        STAR -> R.drawable.ic_star
        HEART -> R.drawable.ic_heart
        else -> 0
    }
    this.setImageResource(res)
}

