package com.example.clonegramtestproject.ui.login.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.TokenData
import com.example.clonegramtestproject.databinding.FragmentVerifyNumberBinding
import com.example.clonegramtestproject.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.firebase.realtime.RealtimeGetter
import com.example.clonegramtestproject.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.ui.login.viewmodels.VerifyViewModel
import com.example.clonegramtestproject.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class VerifyNumberFragment : Fragment(R.layout.fragment_verify_number) {

    private val auth = FirebaseAuth.getInstance()

    private val viewModel by viewModels<VerifyViewModel>()
    private var binding: FragmentVerifyNumberBinding? = null

    private var phoneNumber: String? = null
    private var username: String? = null

    private var recentVerificationId: String? = null
    private var recentToken: PhoneAuthProvider.ForceResendingToken? = null
    private var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var verificationCode: String? = null

    private var sharedPrefs: SharedPreferences? = null
    private var codeLang: String? = null

    private val rtGetter = RealtimeGetter()
    private val rtUser = RealtimeUser()
    private val cmHelper = CMHelper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentVerifyNumberBinding.bind(view)

        sharedPrefs = requireActivity().getSharedPreferences(
            settingsName, Context.MODE_PRIVATE
        )
        sharedPrefs?.let {
            codeLang = it.getString(languagePreferencesName, "")
        }

        phoneNumber = arguments?.getString(
            PHONE
        ).orEmpty()

        setOnClickListener()
        setCallback()
        sendVerifyCode()
        setChronometerTextObserver()
        addTextChangedListener()


        if (!viewModel.countDownTimerStarted) {
            viewModel.setCountDownTimer()
            viewModel.countDownTimerStarted = true
        }
        binding?.tvOnPhoneSend?.append("  $phoneNumber")
    }

    private fun setChronometerTextObserver() {
        binding?.apply {
            viewModel.chronometerLiveData.observe(
                viewLifecycleOwner
            ) {
                chronometer.base = SystemClock.elapsedRealtime() + it
                if (it == 0L) {
                    bSendAgain.isEnabled = true
                }
            }
        }
    }

    private fun sendVerifyCode() {
        auth.setLanguageCode(codeLang.orEmpty())
        lifecycleScope.launch(Dispatchers.IO) {
            callback?.let {
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                    .setPhoneNumber(phoneNumber.orEmpty())
                    .setTimeout(TIMEOUT_MESSAGE, TimeUnit.SECONDS)
                    .setActivity(requireActivity())
                    .setCallbacks(it)
                    .build()
            }?.let {
                PhoneAuthProvider.verifyPhoneNumber(it)
            }
        }
    }

    private fun setCallback() {
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                checkVerificationCode(credential)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                try {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.wrong_code),
                        Snackbar.LENGTH_LONG
                    ).setBackgroundTint(
                        resources.getColor(R.color.red, null)
                    ).show()
                } catch (e: Exception) {
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                recentVerificationId = verificationId
                recentToken = token
            }
        }
    }

    private fun addTextChangedListener() {
        binding?.apply {
            etVerifyPhone.addTextChangedListener {
                if (etVerifyPhone.length() == 6) {
                    bNext.isEnabled = false
                    verificationCode = etVerifyPhone.text.toString()
                    try {
                        val credential = PhoneAuthProvider
                            .getCredential(
                                recentVerificationId.orEmpty(),
                                verificationCode.orEmpty()
                            )
                        checkVerificationCode(credential)
                    } catch (e: Exception) {
                        bNext.isEnabled = true

                        Snackbar.make(
                            requireView(),
                            getString(R.string.wrong_code),
                            Snackbar.LENGTH_LONG
                        ).setBackgroundTint(
                            resources.getColor(
                                R.color.red,
                                null
                            )
                        ).show()

                    }
                }
            }
        }
    }


    private fun checkVerificationCode(credential: PhoneAuthCredential) {
        binding?.apply {
            auth.signInWithCredential(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (etVerifyPhone.text.toString() == "") {
                        etVerifyPhone.setText(credential.smsCode.toString())
                    }
                    val user = it.result.additionalUserInfo
                    etVerifyPhone.setText("")
                    if (!user?.isNewUser!!) {

                        lifecycleScope.launch {
                            username = rtGetter
                                .getUsername(auth.currentUser?.uid.orEmpty())

                            findNavController().navigate(
                                R.id.verify_to_general,
                                bundleOf(
                                    USERNAME to username
                                )
                            )

                            rtUser.setUserToken(
                                TokenData(
                                    cmHelper.getToken(),
                                    System.currentTimeMillis()
                                )
                            )
                        }
                    } else {
                        findNavController().navigate(
                            R.id.verify_to_register,
                            bundleOf(
                                PHONE to phoneNumber
                            )
                        )
                    }
                }
            }
        }
    }

    private fun setOnClickListener() {
        binding?.apply {
            bSendAgain.setOnClickListener {
                sendVerifyCode()
                viewModel.setCountDownTimer()
                bSendAgain.isEnabled = false
            }

            bNext.setOnClickListener {
                if (etVerifyPhone.length() == 6) {
                    verificationCode = etVerifyPhone.text.toString()
                    try {
                        val credential = PhoneAuthProvider
                            .getCredential(
                                recentVerificationId.orEmpty(),
                                verificationCode.orEmpty()
                            )
                        checkVerificationCode(credential)
                    } catch (e: Exception) {
                        showSnackbar(
                            requireView(),
                            getString(R.string.wrong_code),
                            resources.getColor(R.color.red, null)
                        )
                    }
                } else {
                    showSnackbar(
                        requireView(),
                        getString(R.string.code_length),
                        resources.getColor(R.color.red, null)
                    )
                }
            }
        }
    }

    companion object {
        private const val TIMEOUT_MESSAGE = 40L
    }
}