package com.example.clonegramtestproject.ui.message.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.databinding.FragmentPremiumBinding
import com.example.clonegramtestproject.ui.message.viewmodels.PremiumViewModel
import com.example.clonegramtestproject.utils.USER
import org.koin.androidx.viewmodel.ext.android.viewModel


class PremiumFragment : Fragment(R.layout.fragment_premium) {

    private val viewModel by viewModel<PremiumViewModel>()
    private var binding: FragmentPremiumBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPremiumBinding.bind(view)
        setOnClickListeners()
        getArgs()
        setBoughtBtn(viewModel.user?.premium == true)
    }

    private fun setBoughtBtn(premium : Boolean){
        if(premium){
            binding?.buyPremium?.apply {
                isEnabled = false
                alpha = 0.8f
                text = requireContext().getString(R.string.bought_premium)
            }
        }
    }

    private fun getArgs(){
        arguments?.let {
            viewModel.user = it.getParcelable(USER)
        }
    }

    private fun setOnClickListeners(){
        binding?.apply {
            bBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }
}