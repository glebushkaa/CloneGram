package com.example.clonegramtestproject

import android.animation.ObjectAnimator
import android.view.View
import android.widget.Button

class Animations {

   fun openSearchView(
        bSearch: Button,
        searchView: androidx.appcompat.widget.SearchView,
        edgeElement: View
    ) {
        ObjectAnimator.ofFloat(bSearch, View.X, edgeElement.x + 250f).apply {
            ObjectAnimator.ofFloat(bSearch, View.ALPHA, 0f).apply {
                start()
            }
            duration = 300
            start()
        }
        ObjectAnimator.ofFloat(searchView, View.X, edgeElement.x + 250f).apply {
            ObjectAnimator.ofFloat(searchView, View.ALPHA, 1f).apply {
                start()
            }
            duration = 300
            start()
        }
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