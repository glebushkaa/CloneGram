package com.example.clonegramtestproject.ui.login.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.data.TokenData
import com.example.clonegramtestproject.databinding.FragmentRegisterBinding
import com.example.clonegramtestproject.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.firebase.realtime.RealtimeNewUser
import com.example.clonegramtestproject.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.utils.PHONE
import com.example.clonegramtestproject.utils.USERNAME
import com.example.clonegramtestproject.utils.showSnackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val auth = FirebaseAuth.getInstance()

    private var binding: FragmentRegisterBinding? = null

    private var phoneNumber: String? = null
    private var username: String? = null
    private var isRegisterCompleted = false

    private val rtNewUser = RealtimeNewUser()
    private val rtUser = RealtimeUser()
    private val cmHelper = CMHelper()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentRegisterBinding.bind(view)

        arguments?.let {
            phoneNumber = it.getString(PHONE).orEmpty()
        }

        setListeners()
    }

    private fun setListeners() {
        binding?.apply {
            etName.addTextChangedListener {
                if (etName.length() < 2) {
                    etName.error = getString(R.string.too_short_name)
                }
            }

            bRegister.setOnClickListener {
                if (etName.length() > 1) {
                    isRegisterCompleted = true
                    inputDataToDatabase()
                    findNavController().navigate(
                        R.id.register_to_general,
                        bundleOf(
                            USERNAME to username,
                            PHONE to phoneNumber
                        )
                    )
                } else {
                    showSnackbar(
                        requireView(), getString(R.string.enter_correct_name),
                        resources.getColor(R.color.red, null)
                    )
                }
            }
        }
    }

    private fun setUsername() {
        binding?.apply {
            val name = etName.text.toString().trim()
            val surname = etSurname.text.toString().trim()
            username =
                if (surname == "") {
                    name
                } else {
                    "$name $surname"
                }
        }
    }

    private fun inputDataToDatabase() {
        setUsername()
        lifecycleScope.launch {
            rtNewUser.addNewUser(
                CommonModel(
                    username = username.orEmpty(),
                    uid = auth.currentUser?.uid.orEmpty(),
                    phone = phoneNumber.orEmpty()
                )
            )
            rtUser.setUserToken(
                TokenData(
                    cmHelper.getToken(),
                    System.currentTimeMillis()
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isRegisterCompleted) {
            auth.currentUser?.delete()!!
            auth.signOut()
        }
    }
}