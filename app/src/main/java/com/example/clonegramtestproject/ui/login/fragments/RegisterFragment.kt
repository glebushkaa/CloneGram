package com.example.clonegramtestproject.ui.login.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.databinding.FragmentRegisterBinding
import com.example.clonegramtestproject.ui.login.viewmodels.RegisterViewModel
import com.example.clonegramtestproject.utils.PHONE
import com.example.clonegramtestproject.utils.USERNAME
import com.example.clonegramtestproject.utils.showSnackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val viewModel by viewModel<RegisterViewModel>()
    private var binding: FragmentRegisterBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentRegisterBinding.bind(view)

        arguments?.let {
            viewModel.phoneNumber = it.getString(PHONE).orEmpty()
        }

        setListeners()
    }

    private fun setListeners() {
        viewModel.let { vm ->
            binding?.apply {
                etName.addTextChangedListener {
                    if (etName.length() < 2) {
                        etName.error = getString(R.string.too_short_name)
                    }
                }

                bRegister.setOnClickListener {
                    if (etName.length() > 1) {
                        lifecycleScope.launch {
                            setUsername()
                            vm.isRegisterCompleted = true
                            vm.addNewUser()
                            findNavController().navigate(
                                R.id.register_to_general,
                                bundleOf(
                                    USERNAME to vm.username,
                                    PHONE to vm.phoneNumber
                                )
                            )
                        }
                    } else {
                        showSnackbar(
                            requireView(), getString(R.string.enter_correct_name),
                            resources.getColor(R.color.red, null)
                        )
                    }
                }
            }
        }
    }

    private fun setUsername() {
        binding?.apply {
            val name = etName.text.toString().trim()
            val surname = etSurname.text.toString().trim()
            viewModel.username =
                if (surname == "") {
                    name
                } else {
                    "$name $surname"
                }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (!viewModel.isRegisterCompleted) {
            viewModel.deleteUser()
        }
    }
}