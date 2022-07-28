package com.example.clonegramtestproject.ui.message.fragments

import android.net.Uri
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
import com.example.clonegramtestproject.utils.*
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
        private const val PREMIUM_TYPE = "/gif"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fileChooserContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.getSelectedPicture()
        }
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDirectMessageBinding.bind(view)
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

    private fun getArgs() {
        arguments?.let {
            it.getParcelable<CommonModel>(USER)?.let { user ->
                viewModel.user = user
            }
            it.getParcelable<CommonModel>(MY_USER_DATA)?.let { user ->
                viewModel.myUser = user
            }
        }
    }

    private fun setTextForViews() {
        binding.apply {
            viewModel.user?.apply {
                tvDirectTitle.text = username
                tvDirectSubtitle.text = phone
                if(premium && premiumBadge != null){
                    premiumIcon.visibility = View.VISIBLE
                    premiumIcon.setPremiumIcon(premiumBadge)
                }
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
                    nameHolder.text = viewModel.user?.username
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
                dialog?.show(parentFragmentManager,"dialog")
            }
        }

    }

    private fun Uri?.getSelectedPicture() {
        this?.let {
            if (requireContext().contentResolver.getType(it)
                    ?.endsWith(PREMIUM_TYPE) == true && viewModel.myUser?.premium == false
            ) {
                requireView().showSnackbar(
                    text = getString(R.string.gif_image_premium),
                    backgroundTint = resources.getColor(R.color.app_color_red, null)
                )
            } else {
                viewModel.doSendAction(uri = it)
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


