package com.example.clonegramtestproject.ui.login.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Selection
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.FlagsEmojies
import com.example.clonegramtestproject.MainActivity
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CountriesCodes
import com.example.clonegramtestproject.databinding.FragmentLoginBinding
import com.example.clonegramtestproject.utils.CHOSEN_COUNTRY
import com.example.clonegramtestproject.utils.COUNTRY_CODE_ARR
import com.example.clonegramtestproject.utils.PHONE
import com.example.clonegramtestproject.utils.showSoftKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginFragment : Fragment(R.layout.fragment_login) {

    private var binding: FragmentLoginBinding? = null

    private val countryCodeArrayList = arrayListOf<CountriesCodes>()

    private var checkedText = ""
    private var chosenCountry = ""

    private var phoneNumber: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentLoginBinding.bind(view)
        setOnClickListeners()
        addOnTextChangedListener()
        showSoftKeyboard(binding!!.etCode, requireActivity())
        countryCodeArrayList
            .addAll((requireActivity() as MainActivity).countryCodeArrayList)
    }

    override fun onResume() {
        super.onResume()
        chosenCountry = arguments?.getString(
            CHOSEN_COUNTRY
        ).orEmpty()
        binding?.apply {
            if (chosenCountry.isNotEmpty()) {
                etCode.setText(chosenCountry.trim())
                Selection.setSelection(etCode.text, etCode.length())
            }
        }
    }

    private fun addOnTextChangedListener() {
        binding?.apply {
            etCode.addTextChangedListener {
                bCountries.text = ""
                checkedText = "+" + etCode.text.toString().trim()
                addFlagsEmoji()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addFlagsEmoji() {
        lifecycleScope.launch(Dispatchers.IO) {
            for (i in 0 until countryCodeArrayList.size - 1) {
                if (checkedText == countryCodeArrayList[i].dial_code) {
                    lifecycleScope.launch {
                        binding?.apply {
                            bCountries.text =
                                FlagsEmojies.countriesFlags[countryCodeArrayList[i]
                                    .code] + "  " + countryCodeArrayList[i].name
                            if (etCode.length() > 2) {
                                showSoftKeyboard(etPhone, requireActivity())
                            }
                        }
                    }
                    break
                }
            }
        }

    }

    private fun setOnClickListeners() {
        binding?.apply {

            bCountries.setOnClickListener {
                chosenCountry = etCode.text.toString().trim()
                findNavController().navigate(
                    R.id.login_to_country, bundleOf(
                        COUNTRY_CODE_ARR to countryCodeArrayList,
                        CHOSEN_COUNTRY to chosenCountry
                    )
                )
            }

            bSignIn.setOnClickListener {
                phoneNumber = "+" + etCode.text.toString().trim() +
                        etPhone.text.toString().trim()
                findNavController().navigate(
                    R.id.login_to_verify, bundleOf(
                        PHONE to phoneNumber
                    )
                )
            }
        }
    }
}