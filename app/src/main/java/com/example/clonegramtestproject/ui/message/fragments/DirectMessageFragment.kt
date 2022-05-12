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
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.data.LastMessageData
import com.example.clonegramtestproject.data.MessageData
import com.example.clonegramtestproject.databinding.FragmentDirectMessageBinding
import com.example.clonegramtestproject.firebase.realtime.RealtimeChanger
import com.example.clonegramtestproject.firebase.realtime.RealtimeOperator
import com.example.clonegramtestproject.firebase.storage.StorageOperator
import com.example.clonegramtestproject.ui.message.recyclerview.direct.DirectAdapter
import com.example.clonegramtestproject.ui.message.viewmodels.DirectMessageViewModel
import com.example.clonegramtestproject.utils.getSoftInputMode
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DirectMessageFragment : Fragment(R.layout.fragment_direct_message) {

    private val currentUID = FirebaseAuth.getInstance().currentUser?.uid
    private var username: String? = null
    private var phone: String? = null
    private var uid: String? = null
    private var chatUID: String? = null
    private var user: CommonModel? = null

    private val viewModel by viewModels<DirectMessageViewModel>()
    private lateinit var binding: FragmentDirectMessageBinding

    private lateinit var adapter: DirectAdapter

    private var originalSoftInputMode: Int? = null

    private val rtOperator = RealtimeOperator()
    private val rtChanger = RealtimeChanger()
    private val sOperator = StorageOperator()

    private var fileChooserContract: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        fileChooserContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                user?.let { user ->
                    sOperator.pushMessagePicture(
                        it,
                        user, username.orEmpty(), chatUID.orEmpty()
                    )
                }
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDirectMessageBinding.bind(view)

        setBackgroundHeight()
        setVariables()
        setTextForViews()
        initAdapter()
        setListeners()
        setLiveDataObservers()

        user?.userPicture?.let{
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
        viewModel.setMessageListener(chatUID.orEmpty())
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

    private fun setVariables() {
        arguments?.let {
            user = it.getParcelable("user")
        }

        username = user?.username
        phone = user?.phone
        uid = user?.uid
        chatUID = user?.chatUID
    }

    private fun setTextForViews() {
        binding.apply {
            tvDirectTitle.text = username
            tvDirectSubtitle.text = phone
        }
    }

    private fun initAdapter() {
        adapter = DirectAdapter(user, requireContext(), chatUID.orEmpty())
        binding.rvMessages.adapter = adapter
        binding.rvMessages.itemAnimator = null
    }

    private fun setLiveDataObservers() {
        viewModel.messageLiveData.observe(
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
                sendMessage(text)
                etMessageField.setText("")
            }

            bBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }

    }

    private fun sendMessage(text: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            rtOperator.sendMessage(
                userUID = user?.uid.orEmpty(),
                MessageData(
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

            rtChanger.setLastMessageForUser(
                chatUID.orEmpty(),
                LastMessageData(
                    currentUID, text,
                    System.currentTimeMillis()
                )
            )
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.removeMessageListener(uid.orEmpty())
    }

    override fun onDestroy() {
        super.onDestroy()
        originalSoftInputMode?.let {
            requireActivity().window.setSoftInputMode(it)
        }
    }
}


