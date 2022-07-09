package com.example.clonegramtestproject.ui.start.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.ui.start.viewmodel.StartViewModel
import com.example.clonegramtestproject.utils.USER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class StartFragment : Fragment(R.layout.fragment_start) {

    private val viewModel by viewModel<StartViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkUserExists()
    }

    private fun checkUserExists() {
        lifecycleScope.launch {
            if (viewModel.checkLogin()) {
                findNavController().navigate(R.id.start_to_login)
            } else {
                viewModel.getUser()
                checkUsername()
            }
        }
    }


    private fun checkUsername() {
        viewModel.let { vm ->
            vm.user?.username?.let {
                lifecycleScope.launch {
                    vm.setToken()
                    vm.clearOldTokens(viewModel.user)

                    findNavController().navigate(
                        R.id.start_to_message, bundleOf(
                            USER to viewModel.user
                        )
                    )
                }
            }
        } ?: run {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deleteUser()
            }
            findNavController().navigate(R.id.start_to_login)
        }
    }
}