package com.example.clonegramtestproject.ui.message.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.ui.message.dialogs.DirectUserDialog
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.data.models.MessageModel
import com.example.clonegramtestproject.databinding.FragmentDirectMessageBinding
import com.example.clonegramtestproject.ui.Animations
import com.example.clonegramtestproject.ui.message.recyclerview.direct.DirectAdapter
import com.example.clonegramtestproject.ui.message.viewmodels.DirectMessageViewModel
import com.example.clonegramtestproject.utils.MY_USERNAME
import com.example.clonegramtestproject.utils.USER
import com.example.clonegramtestproject.utils.getSoftInputMode
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class DirectMessageFragment : Fragment(R.layout.fragment_direct_message) {

    private val viewModel by viewModel<DirectMessageViewModel>()
    private lateinit var binding: FragmentDirectMessageBinding
    private lateinit var adapter: DirectAdapter

    private var originalSoftInputMode: Int? = null
    private var fileChooserContract: ActivityResultLauncher<String>? = null

    private val animations: Animations by inject()

    private var dialog: DirectUserDialog? = null

    companion object {
        private const val IMAGE_PATH = "image/*"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fileChooserContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri ->
                viewModel.doSendAction(uri = uri)
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
        setDialog()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            viewModel.setMessageListener()
            viewModel.addMessageListener()
        }
    }

    private fun setDialog() {
        viewModel.apply {
            dialog = DirectUserDialog(user)
        }
    }

    private fun setUserPicture() {
        viewModel.user?.userPicture?.let {
            Glide.with(requireContext())
                .load(it)
                .circleCrop()
                .into(binding.profileIcon)
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
                viewModel.user = user
            }
            it.getString(MY_USERNAME)?.let { myUsername ->
                viewModel.myUsername = myUsername
            }
        }
    }

    private fun setTextForViews() {
        binding.apply {
            viewModel.user?.apply {
                tvDirectTitle.text = username
                tvDirectSubtitle.text = phone
            }
        }
    }

    private fun initAdapter() {
        adapter = DirectAdapter(viewModel.currentUID.orEmpty())
        adapter.setOnItemClickedListener(object : DirectAdapter.OnItemClickListener {
            override fun deleteMessageListener(
                userMessageModel: MessageModel,
                prelastMessage: MessageModel?
            ) {
                viewModel.deleteMessage(userMessageModel, prelastMessage)
            }

            override fun deleteMessageForMeListener(
                messageUID: String,
                prelastMessage: MessageModel?
            ) {
                viewModel.deleteMessageForMe(messageUID, prelastMessage)
            }

            override fun editMessage(message: MessageModel,isLastMessage : Boolean) {
                binding.apply {
                    editHolder.visibility = View.VISIBLE
                    animations.showEditHolder(editHolder)
                    etMessageField.setText(message.message)
                    nameHolder.text = viewModel.myUsername
                    messageHolder.text = message.message
                }
                message.message?.length?.let { binding.etMessageField.setSelection(it) }
                viewModel.isEditMessage = true
                viewModel.isEditedMessageLast = isLastMessage
                viewModel.editedMessageInfo = message
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
                fileChooserContract?.launch(IMAGE_PATH)
            }

            etMessageField.addTextChangedListener {
                bSendMessage.isEnabled = etMessageField.text.toString().trim() != ""
            }

            bSendMessage.setOnClickListener {
                val text = etMessageField.text.toString().trim()
                viewModel.doSendAction(text = text)
                etMessageField.setText("")
                editHolder.visibility = View.GONE
            }

            bBack.setOnClickListener {
                findNavController().popBackStack()
            }

            cancelBtn.setOnClickListener {
                nameHolder.text = null
                messageHolder.text = null
                etMessageField.setText("")
                animations.hideEditHolder(editHolder)
                editHolder.visibility = View.GONE
            }

            profileIcon.setOnClickListener {
                dialog?.show(parentFragmentManager, "DIALOG")
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


