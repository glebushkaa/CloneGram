package com.example.clonegramtestproject.ui.message.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.ui.Animations
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.sharedPrefs.SharedPrefsHelper
import com.example.clonegramtestproject.databinding.FragmentSettingsBinding
import com.example.clonegramtestproject.ui.message.SettingsProgressDialog
import com.example.clonegramtestproject.ui.message.viewmodels.SettingsViewModel
import com.example.clonegramtestproject.utils.*
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel by viewModel<SettingsViewModel>()
    private var binding: FragmentSettingsBinding? = null
    private var sharedPreferences: SharedPreferences? = null
    private var fileChooserContract: ActivityResultLauncher<String>? = null

    private val sharedPrefsHelper : SharedPrefsHelper by inject()

    private var dialog = SettingsProgressDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        fileChooserContract = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            it?.let {
                lifecycleScope.launch {
                    changeUserPicture(it)
                }
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSettingsBinding.bind(view)

        setInfoFromSharedPrefs()
        setOnClickListeners()
        addTextChangeListeners()
        getArgs()
        setViews()
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
            }
        }
    }

    private fun getArgs() {
        arguments?.let {
            viewModel.user = it.getParcelable(USER)
        }
    }

    private suspend fun changeUserPicture(uri: Uri) {
        binding?.apply {
            dialog.show(parentFragmentManager,"SETTINGS")
            viewModel.pushUserPicture(uri)
            Glide.with(requireContext()).load(uri)
                .circleCrop()
                .into(bChangePhoto)
            dialog.dismiss()
        }
    }

    private fun setOnClickListeners() {
        setOnClickListenersForThemeButtons()
        setOnClickListenersForLangButtons()

        binding?.apply {

            bChangePhoto.setOnClickListener {
                fileChooserContract?.launch("image/*")
            }

            bBack.setOnClickListener {
                findNavController().popBackStack()
            }

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
                        Uri.parse("https://t.me/mykotlinapps")
                    )
                )
            }

            bChangeBio.setOnClickListener{
                viewModel.changeBio(etBio.text.toString())
            }

            bChangeUsername.setOnClickListener {
                etUsername.text.toString().trim().apply {
                    if (this == viewModel.username) {
                        showSoftKeyboard(etUsername, requireActivity())
                    } else {
                        viewModel.username = this
                        viewModel.changeUsername()
                    }
                }

            }
        }
    }

    private fun setInfoFromSharedPrefs() {
        sharedPreferences = requireActivity().getSharedPreferences(settingsName, Context.MODE_PRIVATE)
        sharedPreferences?.let {
            val lang = sharedPrefsHelper.getLanguageSettings(it, getString(R.string.lang))
            setLangSelectedColor(lang.orEmpty())

            val theme = sharedPrefsHelper.getThemeSettings(it)
            setThemeSelectedIcon(theme.orEmpty())
        }
    }

    private fun setOnClickListenersForLangButtons() {
        binding?.apply {
            bEnglish.setOnClickListener {
                setLanguage(EN)
            }
            bUkrainian.setOnClickListener {
                setLanguage(UKR)
            }
            bRussian.setOnClickListener {
                setLanguage(RU)
            }
        }
    }


    private fun setLanguage(lang: String) {
        setLangSharedPrefs(lang)
        setLangSelectedColor(lang)

        Locale.setDefault(Locale(lang))
        val config = Configuration()
        config.locale = Locale(lang)
        requireContext().resources.updateConfiguration(
            config,
            requireContext().resources.displayMetrics
        )
        showSnackbar(
            requireView(),
            getString(R.string.change_lang_will_be),
            resources.getColor(R.color.white, null)
        )
    }

    private fun setLangSharedPrefs(lang: String) {
        sharedPreferences?.edit()?.putString(
            languagePreferencesName, lang
        )?.apply()
    }

    private fun setLangSelectedColor(
        lang: String
    ) {
        binding?.apply {
            when (lang) {
                EN -> setLangButtonColor(arrayListOf(bRussian, bUkrainian), bEnglish)
                UKR -> setLangButtonColor(arrayListOf(bRussian, bEnglish), bUkrainian)
                RU -> setLangButtonColor(arrayListOf(bEnglish, bUkrainian), bRussian)
            }
        }
    }

    private fun setOnClickListenersForThemeButtons() {
        binding?.apply {
            bYellowTheme.setOnClickListener {
                setThemeColor(YELLOW_THEME)
            }

            bRedTheme.setOnClickListener {
                setThemeColor(RED_THEME)
            }

            bGreenTheme.setOnClickListener {
                setThemeColor(GREEN_THEME)
            }

            bPurpleTheme.setOnClickListener {
                setThemeColor(PURPLE_THEME)
            }

            bBlueTheme.setOnClickListener {
                setThemeColor(BLUE_THEME)
            }
        }
    }

    private fun addTextChangeListeners() {
        binding?.apply {
            etUsername.addTextChangedListener{
                val text = etUsername.text.toString().trim()
                bChangeUsername.isActivated = text != viewModel.user?.username
            }
        }
    }

    private fun setThemeColor(color: String) {
        sharedPreferences?.edit()?.putString(
            themePreferencesName, color
        )?.apply()
        showSnackbar(
            requireView(),
            getString(R.string.change_theme_will_be),
            resources.getColor(R.color.white, null)
        )

        setThemeSelectedIcon(color)
    }

    private fun setThemeSelectedIcon(color: String) {
        binding?.apply {
            val buttonArray = arrayListOf(
                bYellowTheme, bRedTheme, bBlueTheme, bGreenTheme, bPurpleTheme
            )
            when (color) {
                YELLOW_THEME -> setButtonIcon(buttonArray, bYellowTheme)
                GREEN_THEME -> setButtonIcon(buttonArray, bGreenTheme)
                BLUE_THEME -> setButtonIcon(buttonArray, bBlueTheme)
                PURPLE_THEME -> setButtonIcon(buttonArray, bPurpleTheme)
                RED_THEME -> setButtonIcon(buttonArray, bRedTheme)
            }
        }
    }

    private fun setLangButtonColor(
        buttonArray: ArrayList<MaterialButton>,
        button: MaterialButton
    ) {
        buttonArray.forEach {
            it.setBackgroundColor(resources.getColor(R.color.grey_95, null))
        }
        button.setBackgroundColor(resources.getColor(R.color.selected_blue, null))
    }

    private fun setButtonIcon(
        buttonArray: ArrayList<MaterialButton>,
        button: MaterialButton
    ) {
        buttonArray.remove(button)
        buttonArray.forEach {
            it.icon = null
        }
        button.icon = ResourcesCompat
            .getDrawable(resources, R.drawable.ic_check_flag, null)
    }
}