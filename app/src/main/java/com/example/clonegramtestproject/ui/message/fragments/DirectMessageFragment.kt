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
import com.example.clonegramtestproject.data.models.LastMessageModel
import com.example.clonegramtestproject.data.models.MessageModel
import com.example.clonegramtestproject.data.models.NotificationModel
import com.example.clonegramtestproject.databinding.FragmentDirectMessageBinding
import com.example.clonegramtestproject.ui.message.recyclerview.direct.DirectAdapter
import com.example.clonegramtestproject.ui.message.viewmodels.DirectMessageViewModel
import com.example.clonegramtestproject.utils.getSoftInputMode
import kotlinx.coroutines.launch


class DirectMessageFragment : Fragment(R.layout.fragment_direct_message) {


    private val viewModel by viewModels<DirectMessageViewModel>()
    private lateinit var binding: FragmentDirectMessageBinding

    private lateinit var adapter: DirectAdapter

    private var originalSoftInputMode: Int? = null

    private var isEditMessage = false
    private var editedMessageInfo: MessageModel? = null

    private var fileChooserContract: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        fileChooserContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                    viewModel.pushMessagePicture(it)
                (requireActivity().application as MyApp)
                    .retrofit?.let { retrofit ->
                        viewModel.sendNotification(retrofit, getString(R.string.picture))
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

        user?.userPicture?.let {
            Glide.with(requireContext())
                .load(it)
                .circleCrop()
                .into(binding.profileIcon)
        }

        originalSoftInputMode = requireActivity().window?.getSoftInputMode()
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            viewModel.setMessageListener()
            viewModel.addMessageListener()
        }
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

    private fun getArgs(){
        arguments?.let {
            viewModel.setVariables(it.getParcelable("user"))
        }
    }

    private fun setTextForViews() {
        binding.apply {
            tvDirectTitle.text = viewModel.username
            tvDirectSubtitle.text = viewModel.phone
        }
    }

    private fun initAdapter() {
        adapter =
            DirectAdapter(viewModel.messageLiveData,viewModel.user,viewModel.chatUID.orEmpty())
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

        viewModel.messageLiveData.observe(
            viewLifecycleOwner
        ) {
            binding.etMessageField.setText(it?.message)
            isEditMessage = true
            editedMessageInfo = it
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
                if (isEditMessage) {
                    editMessage(text)
                    isEditMessage = false
                    editedMessageInfo = null
                } else {
                    sendMessage(text)
                }
                etMessageField.setText("")

                user?.tokens?.forEach {
                    (requireActivity().application as MyApp)
                        .retrofit?.let { retrofit ->
                            retrofitHelper.sendNotification(
                                retrofit,
                                it.value?.token.toString(),
                                NotificationModel(
                                    username.orEmpty(),
                                    text
                                )
                            )
                        }
                }
            }

            bBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }

    }

    private fun sendMessage(text: String) {
        lifecycleScope.launch {
            rtMessage.sendMessage(
                userUID = user?.uid.orEmpty(),
                MessageModel(
                    uidPermission = arrayListOf(
                        currentUID.orEmpty(),
                        uid.orEmpty()
                    ),
                    uid = currentUID,
                    message = text,
                    timestamp = System.currentTimeMillis(),
                    picture = false
                ),
                chatUID.orEmpty()
            )

            rtMessage.setLastMessage(
                arrayListOf(currentUID.orEmpty(), user?.uid.orEmpty()),
                chatUID.orEmpty(),
                LastMessageModel(
                    message = text,
                    timestamp = System.currentTimeMillis(),
                    picture = false
                )
            )
        }
    }

    private fun editMessage(message: String) {
        lifecycleScope.launch {
            rtMessage.editMessage(
                message,
                chatUID.orEmpty(),
                editedMessageInfo?.messageUid.orEmpty()
            )
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.removeMessageListener(chatUID.orEmpty())
    }

    override fun onDestroy() {
        super.onDestroy()
        originalSoftInputMode?.let {
            requireActivity().window.setSoftInputMode(it)
        }
    }
}


