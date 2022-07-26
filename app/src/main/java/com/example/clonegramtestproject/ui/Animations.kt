package com.example.clonegramtestproject.ui

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
        toolbar : View? = null
    ) {
        ObjectAnimator.ofFloat(bSearch, View.X, toolbar!!.width - 150f).apply {
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

    fun showEditHolder(item: View){
        ObjectAnimator.ofFloat(item,View.ROTATION,item.z).apply {
            ObjectAnimator.ofFloat(item,View.ALPHA,1f).apply {
                start()
            }
            duration = 10000
            start()
        }
    }

    fun hideEditHolder(item: View){
        ObjectAnimator.ofFloat(item,View.ROTATION,item.z-100f).apply {
            ObjectAnimator.ofFloat(item,View.ALPHA,0f).apply {
                start()
            }
            duration = 100000
            start()
        }
    }
}