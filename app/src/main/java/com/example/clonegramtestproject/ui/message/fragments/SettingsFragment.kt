package com.example.clonegramtestproject.ui.message.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.changeIconButton
import com.example.clonegramtestproject.data.language.LanguageHelper
import com.example.clonegramtestproject.data.sharedPrefs.PrefsHelper
import com.example.clonegramtestproject.databinding.FragmentSettingsBinding
import com.example.clonegramtestproject.setSelectedColorTheme
import com.example.clonegramtestproject.setSelectedIcon
import com.example.clonegramtestproject.ui.message.dialogs.SettingsProgressDialog
import com.example.clonegramtestproject.ui.message.viewmodels.SettingsViewModel
import com.example.clonegramtestproject.utils.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel by viewModel<SettingsViewModel>()
    private var binding: FragmentSettingsBinding? = null
    private var fileChooserContract: ActivityResultLauncher<String>? = null

    private val prefsHelper: PrefsHelper by inject()
    private val langHelper: LanguageHelper by inject()

    private var dialog = SettingsProgressDialog()

    companion object {
        private const val ASK_URL = "https://t.me/mykotlinapps"
        private const val PREMIUM_TYPE = "/gif"
        private const val IMAGE_PATH = "image/*"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fileChooserContract = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { it.getSelectedPicture() }
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSettingsBinding.bind(view)
        setInfoFromSharedPrefs()
        setCheckedLangListener()
        addTextChangeListeners()
        getArgs()
        setViews()
        setPremiumFunctions(viewModel.user?.premium == true)
        setClickListeners()
        setThemeClickListeners()
        setIconClickListener()
    }

    private fun setPremiumFunctions(premium: Boolean) {
        binding?.apply {
            if (premium) {
                changerIconPremium.root.visibility = View.VISIBLE

                changerThemeContainer.apply {
                    val btnList = arrayListOf(bOrangeTheme, bGreenTheme)
                    btnList.forEach {
                        it.apply {
                            icon = null
                            alpha = 1f
                            isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun setClickListeners() {
        binding?.apply {
            bChangePhoto.setOnClickListener { fileChooserContract?.launch(IMAGE_PATH) }
            bBack.setOnClickListener { findNavController().popBackStack() }
            bSignOut.setOnClickListener {
                viewModel.signOut()
                findNavController().navigate(R.id.settings_to_login)
            }
            bDelete.setOnClickListener {
                viewModel.deleteUser()
                findNavController().navigate(R.id.settings_to_login)
            }
            bAskQuestion.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(ASK_URL)
                    )
                )
            }
            bChangeBio.setOnClickListener { viewModel.changeBio(etBio.text.toString()) }
            bChangeUsername.setOnClickListener { changeUsername() }

        }
    }

    private fun setThemeClickListeners() {
        binding?.changerThemeContainer?.apply {
            bGreenTheme.setOnClickListener { setThemeColor(GREEN_THEME) }
            bYellowTheme.setOnClickListener { setThemeColor(YELLOW_THEME) }
            bRedTheme.setOnClickListener { setThemeColor(RED_THEME) }
            bPurpleTheme.setOnClickListener { setThemeColor(PURPLE_THEME) }
            bDarkPurpleTheme.setOnClickListener { setThemeColor(DARK_PURPLE_THEME) }
            bOrangeTheme.setOnClickListener { setThemeColor(ORANGE_THEME) }
            bBlueTheme.setOnClickListener { setThemeColor(BLUE_THEME) }
        }
    }

    private fun setIconClickListener() {
        binding?.changerIconPremium?.apply {
            disabled.setOnClickListener {
                changeIconButton(disabled.id)
                viewModel.setPremiumIcon(DISABLED)
            }
            fire.setOnClickListener {
                changeIconButton(fire.id)
                viewModel.setPremiumIcon(FIRE)
            }
            star.setOnClickListener {
                changeIconButton(star.id)
                viewModel.setPremiumIcon(STAR)
            }
            heart.setOnClickListener {
                changeIconButton(heart.id)
                viewModel.setPremiumIcon(HEART)
            }
        }
    }

    private fun setViews() {
        binding?.apply {
            viewModel.apply {
                user?.userPicture?.let {
                    Glide.with(requireContext())
                        .load(it)
                        .circleCrop()
                        .into(bChangePhoto)
                }
                etUsername.setText(user?.username)
                tvPhone.text = phoneNumber
                etBio.setText(user?.userBio)
                user?.premiumBadge?.let { binding?.changerIconPremium?.setSelectedIcon(it) }
            }
        }
    }

    private fun getArgs() {
        arguments?.let {
            viewModel.user = it.getParcelable(USER)
        }
    }

    private fun changeUserPicture(uri: Uri) {
        lifecycleScope.launch {
            binding?.apply {
                dialog.show(parentFragmentManager, "settings")
                viewModel.pushUserPicture(uri)
                Glide.with(requireContext()).load(uri)
                    .circleCrop()
                    .into(bChangePhoto)
                dialog.dismiss()
            }
        }
    }

    private fun changeUsername() {
        binding?.apply {
            etUsername.text.toString().trim().apply {
                if (this == viewModel.username) {
                    requireActivity().showSoftKeyboard(etUsername)
                } else {
                    viewModel.username = this
                    viewModel.changeUsername()
                }
            }
        }
    }

    private fun setInfoFromSharedPrefs() {
        val lang = prefsHelper.getLanguageSettings(getString(R.string.lang))
        binding?.changeLanguageItem?.langGroup?.setCheckedLangButton(lang.orEmpty())

        val theme = prefsHelper.getThemeSettings()
        binding?.changerThemeContainer?.setSelectedColorTheme(theme.orEmpty())
    }

    private fun setCheckedLangListener() {
        binding?.changeLanguageItem?.apply {
            langGroup.setOnCheckedChangeListener { _, id ->
                val lang = when (id) {
                    R.id.ukrainianBtn -> UKR
                    R.id.russianBtn -> RU
                    else -> EN
                }
                setLanguage(lang)
            }
        }
    }


    private fun setLanguage(lang: String) {
        prefsHelper.editLang(lang)
        langHelper.setLanguage(lang, requireContext())
        requireView().showSnackbar(
            text = getString(R.string.change_lang_will_be),
            textColor = resources.getColor(R.color.white, null)
        )
    }

    private fun addTextChangeListeners() {
        binding?.apply {
            etUsername.addTextChangedListener {
                val text = etUsername.text.toString().trim()
                bChangeUsername.isActivated = text != viewModel.user?.username
            }
        }
    }

    private fun setThemeColor(color: String) {
        prefsHelper.editTheme(color)
        binding?.changerThemeContainer?.setSelectedColorTheme(color)
        requireView().showSnackbar(
            text = getString(R.string.change_theme_will_be),
            textColor = resources.getColor(R.color.white, null)
        )
    }

    private fun Uri?.getSelectedPicture() {
        this?.let {
            if (requireContext().contentResolver.getType(it)
                    ?.endsWith(PREMIUM_TYPE) == true && viewModel.user?.premium == false
            ) {
                requireView().showSnackbar(
                    text = getString(R.string.gif_image_premium),
                    backgroundTint = resources.getColor(R.color.app_color_red, null)
                )
            } else {
                changeUserPicture(it)
            }
        }
    }
}