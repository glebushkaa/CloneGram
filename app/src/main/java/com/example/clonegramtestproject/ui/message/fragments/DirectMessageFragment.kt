package com.example.clonegramtestproject.ui.message.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.MyApp
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.models.MessageModel
import com.example.clonegramtestproject.databinding.FragmentDirectMessageBinding
import com.example.clonegramtestproject.ui.message.recyclerview.direct.DirectAdapter
import com.example.clonegramtestproject.ui.message.viewmodels.DirectMessageViewModel
import com.example.clonegramtestproject.utils.USER
import com.example.clonegramtestproject.utils.getSoftInputMode
import com.example.clonegramtestproject.utils.showToast
import kotlinx.coroutines.launch


class DirectMessageFragment : Fragment(R.layout.fragment_direct_message) {

    private val viewModel by viewModels<DirectMessageViewModel>()
    private lateinit var binding: FragmentDirectMessageBinding
    private lateinit var adapter: DirectAdapter

    private var originalSoftInputMode: Int? = null
    private var fileChooserContract: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        fileChooserContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                lifecycleScope.launch {
                    viewModel.pushMessagePicture(it)
                    viewModel.changeLastMessage(null, true)
                    (requireActivity().application as MyApp)
                        .retrofit?.let { retrofit ->
                            viewModel.sendNotification(retrofit, getString(R.string.picture))
                        }

                }
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDirectMessageBinding.bind(view)

        setBackgroundHeight()
        getArgs()
        setTextForViews()
        initAdapter()
        setListeners()
        setLiveDataObservers()
        setUserPicture()
        setSoftInputMode()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            viewModel.setMessageListener()
            viewModel.addMessageListener()
        }
    }

    private fun setUserPicture() {
        viewModel.getUserPicture()?.let {
            Glide.with(requireContext()).load(it).circleCrop().into(binding.profileIcon)
        }
    }

    private fun setSoftInputMode() {
        originalSoftInputMode = requireActivity().window?.getSoftInputMode()
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    private fun setBackgroundHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = requireContext().display
            display?.height?.let {
                binding.directBackground.layoutParams.height = it
            }
        } else {
            val display = requireActivity().windowManager.defaultDisplay
            binding.directBackground.layoutParams.height = display.height
        }
    }

    private fun getArgs() {
        arguments?.let {
            it.getParcelable<CommonModel>(USER)?.let { user ->
                viewModel.setVariables(user)
            }
        }
    }

    private fun setTextForViews() {
        binding.apply {
            tvDirectTitle.text = viewModel.username
            tvDirectSubtitle.text = viewModel.phone
        }
    }

    private fun initAdapter() {
        adapter = DirectAdapter(viewModel.currentUID.orEmpty())
        adapter.setOnItemClickedListener(object : DirectAdapter.OnItemClickListener {
            override fun deleteMessageListener(
                userMessageModel: MessageModel,
                messageList: ArrayList<MessageModel>
            ) {
                lifecycleScope.launch {
                    viewModel.deleteMessage(userMessageModel, messageList)
                }
            }

            override fun deleteMessageForMeListener(messageUID: String) {
                lifecycleScope.launch {
                    viewModel.deleteMessageForMe(messageUID)
                }
            }

            override fun editMessage(message: MessageModel) {
                lifecycleScope.launch {
                    binding.etMessageField.setText(message.message)
                    message.message?.length?.let { binding.etMessageField.setSelection(it) }
                    viewModel.isEditMessage = true
                    viewModel.editedMessageInfo = message
                }
            }

        })
        binding.rvMessages.adapter = adapter
        binding.rvMessages.itemAnimator = null
    }

    private fun setLiveDataObservers() {
        viewModel.messagesLiveData.observe(
            viewLifecycleOwner
        ) {
            adapter.setData(it)
            binding.rvMessages.scrollToPosition(it.size - 1)
        }
    }


    private fun setListeners() {
        binding.apply {

            bPhotoPicker.setOnClickListener {
                fileChooserContract?.launch("image/*")
            }

            etMessageField.addTextChangedListener {
                bSendMessage.isEnabled = etMessageField.text.toString().trim() != ""
            }

            bSendMessage.setOnClickListener {
                val text = etMessageField.text.toString().trim()
                viewModel.doSendAction(text)
                etMessageField.setText("")

                lifecycleScope.launch {
                    (requireActivity().application as MyApp)
                        .retrofit?.let { retrofit ->
                            viewModel.sendNotification(retrofit, text)
                        }
                }
            }

            bBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        viewModel.removeMessageListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        originalSoftInputMode?.let {
            requireActivity().window.setSoftInputMode(it)
        }
    }
}


