package com.example.clonegramtestproject.ui.message.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.example.clonegramtestproject.data.language.LanguageHelper
import com.example.clonegramtestproject.data.sharedPrefs.SharedPrefsHelper
import com.example.clonegramtestproject.databinding.FragmentSettingsBinding
import com.example.clonegramtestproject.ui.message.SettingsProgressDialog
import com.example.clonegramtestproject.ui.message.viewmodels.SettingsViewModel
import com.example.clonegramtestproject.utils.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel by viewModel<SettingsViewModel>()
    private var binding: FragmentSettingsBinding? = null
    private var sharedPreferences: SharedPreferences? = null
    private var fileChooserContract: ActivityResultLauncher<String>? = null

    private val sharedPrefsHelper: SharedPrefsHelper by inject()
    private val langHelper: LanguageHelper by inject()

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
            dialog.show(parentFragmentManager, "SETTINGS")
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

            bChangeBio.setOnClickListener {
                viewModel.changeBio(etBio.text.toString())
            }

            bChangeUsername.setOnClickListener {
                changeUsername()
            }
        }
    }

    private fun changeUsername() {
        binding?.apply {
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

    private fun setInfoFromSharedPrefs() {
        sharedPreferences =
            requireActivity().getSharedPreferences(
                settingsName, Context.MODE_PRIVATE
            )
        sharedPreferences?.let {
            val lang = sharedPrefsHelper.getLanguageSettings(it, getString(R.string.lang))
            setCheckedLangButton(lang.orEmpty())

            val theme = sharedPrefsHelper.getThemeSettings(it)
            setSelectedColorTheme(theme.orEmpty())
        }
    }

    private fun setCheckedLangButton(lang: String) {
        binding?.changeLanguageItem?.apply {
            when (lang) {
                UKR -> langGroup.check(R.id.ukrainianBtn)
                RU -> langGroup.check(R.id.russianBtn)
                EN -> langGroup.check(R.id.englishBtn)
            }
        }
    }

    private fun setOnClickListenersForLangButtons() {
        binding?.changeLanguageItem?.apply {
            langGroup.setOnCheckedChangeListener { _, id ->
                when (id) {
                    R.id.ukrainianBtn -> setLanguage(UKR)
                    R.id.englishBtn -> setLanguage(EN)
                    R.id.russianBtn -> setLanguage(RU)
                }
            }
        }
    }


    private fun setLanguage(lang: String) {
        setLangSharedPrefs(lang)
        langHelper.setLanguage(lang, requireContext())
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

    private fun setOnClickListenersForThemeButtons() {
        binding?.changerThemeContainer?.apply {
            bYellowTheme.setOnClickListener {
                setThemeColor(YELLOW_THEME)
            }
            bRedTheme.setOnClickListener {
                setThemeColor(RED_THEME)
            }

            bDarkPurpleTheme.setOnClickListener {
                setThemeColor(DARK_PURPLE_THEME)
            }

            bPurpleTheme.setOnClickListener {
                setThemeColor(PURPLE_THEME)
            }

            bBlueTheme.setOnClickListener {
                setThemeColor(BLUE_THEME)
            }
            bOrangeTheme.setOnClickListener {
                setThemeColor(ORANGE_THEME)
            }
        }
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
        sharedPreferences?.edit()?.putString(
            themePreferencesName, color
        )?.apply()
        setSelectedColorTheme(color)
        showSnackbar(
            requireView(),
            getString(R.string.change_theme_will_be),
            resources.getColor(R.color.white, null)
        )
    }

    private fun setSelectedColorTheme(theme : String){
        binding?.changerThemeContainer?.apply {
            val id = when(theme){
                YELLOW_THEME -> bYellowTheme.id
                RED_THEME -> bRedTheme.id
                BLUE_THEME -> bBlueTheme.id
                PURPLE_THEME -> bPurpleTheme.id
                DARK_PURPLE_THEME -> bDarkPurpleTheme.id
                else -> bOrangeTheme.id
            }
            changeThemeButton(id)
        }
    }

    private fun changeThemeButton(btnId : Int){
        binding?.changerThemeContainer?.apply {
            val btnList = arrayListOf(
                bYellowTheme,bOrangeTheme,bPurpleTheme,
                bBlueTheme,bRedTheme,bDarkPurpleTheme
            )

            btnList.forEach {
                if(it.id != btnId){
                    it.changeStrokeWidth(0)
                }else{
                    it.changeStrokeWidth(10)
                }
            }
        }
    }
}