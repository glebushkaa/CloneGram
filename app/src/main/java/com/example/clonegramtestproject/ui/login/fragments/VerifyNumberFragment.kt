package com.example.clonegramtestproject.ui.login.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.models.TokenModel
import com.example.clonegramtestproject.databinding.FragmentVerifyNumberBinding
import com.example.clonegramtestproject.data.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeGetter
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
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
import java.util.concurrent.TimeUnit

class VerifyNumberFragment : Fragment(R.layout.fragment_verify_number) {
    private val viewModel by viewModels<VerifyViewModel>()
    private var binding: FragmentVerifyNumberBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentVerifyNumberBinding.bind(view)

        viewModel.phoneNumber = arguments?.getString(PHONE)
        setOnClickListener()
        setCallback()
        sendVerifyCode()
        addTextChangedListener()
        setChronometerTextObserver()
        setCountdownTimer()
        binding?.tvOnPhoneSend?.append("  ${viewModel.phoneNumber}")
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
        viewModel.setAuthLang(getSharedPrefs())
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.apply {
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
    }


    private fun addTextChangedListener() {
        binding?.apply {
            etVerifyPhone.addTextChangedListener {
                if (etVerifyPhone.length() == 6) {
                    verifyCode()
                }
            }
        }
    }

    private fun setCallback() {
        viewModel.callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                checkVerificationCode(credential)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                try {
                    Snackbar.make(
                        requireView(), getString(R.string.wrong_code),
                        Snackbar.LENGTH_LONG
                    ).setBackgroundTint(
                        resources.getColor(R.color.red, null)
                    ).show()
                } catch (e: Exception) {
                    Log.e("Snackbar Failed",e.message.orEmpty())
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                viewModel.apply {
                    recentVerificationId = verificationId
                    recentToken = token
                }
            }
        }
    }

    private fun checkVerificationCode(credential: PhoneAuthCredential) {
        binding?.apply {
                lifecycleScope.launch {
                    viewModel.signInWithCredential(credential)?.let {
                        if (etVerifyPhone.text.toString() == "") {
                            etVerifyPhone.setText(credential.smsCode.toString())
                        }
                        etVerifyPhone.setText("")
                        if (!it.additionalUserInfo?.isNewUser!!) {
                            viewModel.setUsername()
                            viewModel.setToken()
                            findNavController().navigate(
                                R.id.verify_to_general,
                                bundleOf(
                                    USERNAME to viewModel.username
                                )
                            )
                        } else {
                            findNavController().navigate(
                                R.id.verify_to_register,
                                bundleOf(
                                    PHONE to viewModel.phoneNumber
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
                    verifyCode()
                } else {
                    showSnackbar(
                        requireView(), getString(R.string.code_length),
                        resources.getColor(R.color.red, null)
                    )
                }
            }
        }
    }

    private fun getSharedPrefs(): SharedPreferences =
        requireActivity().getSharedPreferences(
            settingsName, Context.MODE_PRIVATE
        )

    private fun verifyCode() {
        viewModel.verificationCode = binding?.etVerifyPhone?.text.toString()
        binding?.bNext?.isEnabled = false
        try {
            checkVerificationCode(viewModel.getCredential())
        } catch (e: Exception) {
            binding?.bNext?.isEnabled = true
            showSnackbar(
                requireView(), getString(R.string.wrong_code),
                resources.getColor(R.color.red, null)
            )
        }
    }

    private fun setCountdownTimer(){
        if (!viewModel.countDownTimerStarted) {
            viewModel.setCountDownTimer()
            viewModel.countDownTimerStarted = true
        }
    }


    companion object {
        private const val TIMEOUT_MESSAGE = 40L
    }
}