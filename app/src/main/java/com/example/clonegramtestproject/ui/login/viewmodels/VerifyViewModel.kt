package com.example.clonegramtestproject.ui.login.viewmodels

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VerifyViewModel : ViewModel() {
    var chronometerLiveData = MutableLiveData<Long>()
    var countDownTime = 60L
    var countDownTimerStarted = false

    fun setCountDownTimer(){
        val timer = object : CountDownTimer(
            countDownTime * 1000,1000){
            override fun onTick(millisUntilFinished: Long) {
                chronometerLiveData.value = millisUntilFinished
            }
            override fun onFinish() {
                chronometerLiveData.value = 0L
            }
        }
        timer.start()
    }
    
}