package com.example.clonegramtestproject.ui.message

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.clonegramtestproject.databinding.SettingsProgressDialogBinding

class SettingsProgressDialog : DialogFragment(){

    private var binding: SettingsProgressDialogBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = SettingsProgressDialogBinding.inflate(layoutInflater)
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val dialog = builder.setView(binding?.root).create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

}