package com.example.clonegramtestproject

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Button

class Animations {

    fun openSearchView(
        bSearch: Button,
        searchView: androidx.appcompat.widget.SearchView,
        edgeElement: View
    ) {
        val animSet = AnimatorSet() // use animator set with more then 1 animation
        val a = ObjectAnimator.ofFloat(bSearch, View.X, edgeElement.x + 250f).apply {
            duration = 300
        }
        val b = ObjectAnimator.ofFloat(bSearch, View.ALPHA, 0f).apply {

        }
        val c = ObjectAnimator.ofFloat(searchView, View.X, edgeElement.x + 250f).apply {
            duration = 300
        }
        val d = ObjectAnimator.ofFloat(searchView, View.ALPHA, 1f).apply {

        }
        animSet.play(a).with(b).with(c).with(d)
    }

    fun closeSearchView(
        bSearch: Button,
        searchView: androidx.appcompat.widget.SearchView,
        edgeElement: View
    ) {
        ObjectAnimator.ofFloat(bSearch, View.X, edgeElement.x - 150f).apply {
            ObjectAnimator.ofFloat(bSearch, View.ALPHA, 1f).apply {
                start()
            }
            duration = 300
            start()
        }
        ObjectAnimator.ofFloat(searchView, View.X, 1000f).apply {
            ObjectAnimator.ofFloat(searchView, View.ALPHA, 0f).apply {
                start()
            }
            duration = 300
            start()
        }
    }

    fun showItem(item : View,alpha : Float){
       ObjectAnimator.ofFloat(item,View.ALPHA,alpha).apply {
           duration = 500
            start()
        }
    }

    fun hideItem(item : View){
        ObjectAnimator.ofFloat(item,View.ALPHA,0f).apply {
            duration = 500
            start()
        }
    }
}