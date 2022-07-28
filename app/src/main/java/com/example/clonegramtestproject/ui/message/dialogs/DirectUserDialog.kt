package com.example.clonegramtestproject.ui.message.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.databinding.UserDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DirectUserDialog(
    val user: CommonModel? = null
) : BottomSheetDialogFragment() {

    private var binding: UserDialogBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = UserDialogBinding.inflate(inflater)
        setViews()
        return binding?.root
    }

    private fun setViews() {
        binding?.apply {
            user?.apply {
                usernameField.text = username
                phoneField.text = phone
                bioField.text = userBio
                context?.let { context ->
                    userPicture?.let { image ->
                        Glide.with(context).load(image).circleCrop().into(profileIcon)
                    }
                }
            }
        }
    }
}