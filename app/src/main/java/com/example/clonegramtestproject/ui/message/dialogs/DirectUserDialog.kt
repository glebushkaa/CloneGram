package com.example.clonegramtestproject.ui.message.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.databinding.UserDialogBinding

class DirectUserDialog(
    val user : CommonModel? = null
) : DialogFragment() {

    private var binding: UserDialogBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = UserDialogBinding.inflate(layoutInflater)
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val dialog = builder.setView(binding?.root).create()
        dialog?.window?.setBackgroundDrawableResource(R.drawable.shape_user_card)
        setViews()
        dialog.setClickListener()
        return dialog
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

    private fun AlertDialog.setClickListener() {
        binding?.finishBtn?.setOnClickListener {
            cancel()
        }
    }
}