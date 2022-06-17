package com.example.clonegramtestproject.ui.login.viewmodels

import android.content.SharedPreferences
import android.os.CountDownTimer
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeGetter
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.data.models.TokenModel
import com.example.clonegramtestproject.data.sharedPrefs.SharedPrefsHelper
import com.example.clonegramtestproject.ui.login.fragments.VerifyNumberFragment
import com.example.clonegramtestproject.utils.PHONE
import com.example.clonegramtestproject.utils.USERNAME
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class VerifyViewModel : ViewModel() {

    val auth = FirebaseAuth.getInstance()

    var phoneNumber: String? = null
    var username: String? = null

    var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    var recentVerificationId: String? = null
    var recentToken: PhoneAuthProvider.ForceResendingToken? = null
    var verificationCode: String? = null

    private val rtGetter = RealtimeGetter()
    private val rtUser = RealtimeUser()
    private val cmHelper = CMHelper()

    var chronometerLiveData = MutableLiveData<Long>()
    var countDownTime = 60L
    var countDownTimerStarted = false

    private val sharedPrefsHelper = SharedPrefsHelper()

    fun setCountDownTimer() {
        val timer = object : CountDownTimer(
            countDownTime * 1000, 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                chronometerLiveData.value = millisUntilFinished
            }

            override fun onFinish() {
                chronometerLiveData.value = 0L
            }
        }
        timer.start()
    }

    fun setAuthLang(lang : String) {
        auth.setLanguageCode(lang)
    }

    suspend fun signInWithCredential(credential: PhoneAuthCredential) =
        suspendCoroutine<AuthResult?> { emitter ->
            auth.signInWithCredential(credential).addOnSuccessListener {
                emitter.resume(it)
            }.addOnFailureListener {
                emitter.resume(null)
            }
        }

    suspend fun setToken() {
        withContext(Dispatchers.IO) {
            rtUser.setUserToken(
                TokenModel(
                    cmHelper.getToken(),
                    System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun setUsername() {
        withContext(Dispatchers.IO) {
            username = rtGetter
                .getUsername(auth.currentUser?.uid.orEmpty())
        }
    }

    fun getCredential() = PhoneAuthProvider.getCredential(
        recentVerificationId.orEmpty(),
        verificationCode.orEmpty()
    )

}