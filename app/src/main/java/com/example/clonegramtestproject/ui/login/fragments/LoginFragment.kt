package com.example.clonegramtestproject.ui.login.fragments

import android.os.Bundle
import android.text.Selection
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.ui.activity.activity.MainActivity
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.databinding.FragmentLoginBinding
import com.example.clonegramtestproject.ui.login.viewmodels.LoginViewModel
import com.example.clonegramtestproject.utils.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel by viewModel<LoginViewModel>()

    private var binding: FragmentLoginBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentLoginBinding.bind(view)
        setOnClickListeners()
        addOnTextChangedListener()
        showSoftKeyboard(binding!!.etCode, requireActivity())

        viewModel.fillCountryCodesList(
            (requireActivity() as MainActivity)
                .getCountryCodeList()
        )

    }

    override fun onResume() {
        super.onResume()
        binding?.apply {
            viewModel.apply {
                chosenCountry = arguments?.getString(
                    CHOSEN_COUNTRY
                )
                chosenCountry?.let { country ->
                    etCode.setText(country.trim())
                    Selection.setSelection(etCode.text, etCode.length())
                }
            }
        }
    }

    private fun addOnTextChangedListener() {
        binding?.apply {
            etCode.addTextChangedListener {
                bCountries.text = ""
                viewModel.code = "+" + etCode.text.toString().trim()
                setCountry()
            }
        }
    }

    private fun setCountry() {
        binding?.apply {
            lifecycleScope.launch {
                viewModel.setCountry()?.let {
                    bCountries.text = it
                    if (etCode.length() > 2) {
                        showSoftKeyboard(etPhone, requireActivity())
                    }
                }
            }
        }
    }


    private fun setOnClickListeners() {
        binding?.apply {
            viewModel.let { vm ->
                bCountries.setOnClickListener {
                    vm.chosenCountry = etCode.text.toString().trim()
                    findNavController().navigate(
                        R.id.login_to_country, bundleOf(
                            COUNTRY_CODE_ARR to vm.countryCodeArrayList,
                            CHOSEN_COUNTRY to vm.chosenCountry
                        )
                    )
                }

                bSignIn.setOnClickListener {
                    arguments?.putString(
                        CHOSEN_COUNTRY, vm.code?.removePrefix("+")
                    )
                    vm.phoneNumber = "+" + etCode.text.toString().trim() +
                            etPhone.text.toString().trim()
                    findNavController().navigate(
                        R.id.login_to_verify, bundleOf(
                            PHONE to vm.phoneNumber,
                        )
                    )
                }
            }
        }
    }
}