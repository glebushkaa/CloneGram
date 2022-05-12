package com.example.clonegramtestproject.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

fun Context.showToast(text : String){
   Toast.makeText(this,text, Toast.LENGTH_SHORT).show()
}

fun showSnackbar(view: View,text: String,color : Int){
    Snackbar.make(view,text,Snackbar.LENGTH_LONG).setTextColor(color).show()
}

fun showSoftKeyboard(view: View,activity : Activity) {
    view.requestFocus()
    val imm: InputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun closeSoftKeyboard(view: View,activity : Activity) {
    val imm: InputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken,0)
}

fun Window.getSoftInputMode() : Int {
    return attributes.softInputMode
}