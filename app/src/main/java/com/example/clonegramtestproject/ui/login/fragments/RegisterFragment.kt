package com.example.clonegramtestproject.ui.login.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.databinding.FragmentRegisterBinding
import com.example.clonegramtestproject.firebase.realtime.RealtimeChanger
import com.example.clonegramtestproject.utils.showSnackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val auth = FirebaseAuth.getInstance()

    private var binding: FragmentRegisterBinding? = null

    private var phoneNumber: String? = null
    private var username: String? = null
    private var isRegisterCompleted = false

    private val firebaseUserChanger = RealtimeChanger()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentRegisterBinding.bind(view)

        arguments?.let {
            phoneNumber = it.getString("phone").orEmpty()
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
                            "username" to username,
                            "phone" to phoneNumber
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

        lifecycleScope.launch(Dispatchers.IO) {
            firebaseUserChanger.addNewUser(
                username.orEmpty(),
                auth.currentUser?.uid.orEmpty(),
                phoneNumber.orEmpty()
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