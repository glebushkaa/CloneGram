package com.example.clonegramtestproject.ui.start.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.ui.start.viewmodel.StartViewModel
import com.example.clonegramtestproject.utils.USER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class StartFragment : Fragment(R.layout.fragment_start) {
    private val viewModel by viewModels<StartViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkIsUserExist()
    }

    private fun checkIsUserExist() {
        ?: run {
            findNavController().navigate(R.id.start_to_login)
        }
    }


    private fun checkHasUserName() {
        viewModel.user?.username?.let {
            lifecycleScope.launch {
                viewModel.let {
                    it.setToken()
                    it.clearOldTokens(viewModel.user)
                }

                findNavController().navigate(
                    R.id.start_to_message, bundleOf(
                        USER to viewModel.user
                    )
                )
            }
        } ?: run {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deleteUser()
            }
            findNavController().navigate(R.id.start_to_login)
        }
    }

}