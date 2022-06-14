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
import com.example.clonegramtestproject.Animations
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.databinding.FragmentSettingsBinding
import com.example.clonegramtestproject.data.firebase.cloudMessaging.CMHelper
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeUser
import com.example.clonegramtestproject.data.firebase.storage.StorageOperator
import com.example.clonegramtestproject.utils.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var binding: FragmentSettingsBinding? = null

    private var phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
    private var user: CommonModel? = null
    private var username: String? = null

    private val rtUser = RealtimeUser()
    private val cmHelper = CMHelper()

    private var sharedPreferences: SharedPreferences? = null

    private var fileChooserContract: ActivityResultLauncher<String>? = null

    private var storageOperator = StorageOperator()
    private var animator = Animations()

    override fun onCreate(savedInstanceState: Bundle?) {
        fileChooserContract = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            if (it != null) {
                binding?.apply {
                    lifecycleScope.launch {
                        animator.showItem(darkBackground, 0.6f)
                        animator.showItem(progressBar, 1f)
                        changeIsEnabledAllViews(true)

                        storageOperator.pushUserPicture(it)

                        Glide.with(requireContext())
                            .load(it)
                            .circleCrop()
                            .into(bChangePhoto)

                        animator.hideItem(darkBackground)
                        animator.hideItem(progressBar)
                        changeIsEnabledAllViews(false)
                    }
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
        arguments?.let {
            user = it.getParcelable("user")
        }
        binding?.apply {
            user?.userPicture?.let {
                Glide.with(requireContext())
                    .load(it)
                    .circleCrop()
                    .into(bChangePhoto)
            }
            username = user?.username

            etUsername.setText(username)
            tvPhone.text = phoneNumber
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
                lifecycleScope.launch {

                    cmHelper.getToken().let {
                        rtUser.deleteToken(it)
                    }

                    rtUser.signOut()
                    findNavController().navigate(R.id.settings_to_login)
                }
            }

            bDelete.setOnClickListener {
               lifecycleScope.launch {
                   rtUser.deleteUser()
                   findNavController().navigate(R.id.settings_to_login)
               }
            }

            bAskQuestion.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://t.me/mykotlinapps")
                    )
                )
            }


            bChangeUsername.setOnClickListener {
                if (etUsername.text.toString() == username) {
                    showSoftKeyboard(etUsername, requireActivity())
                } else {
                    username = etUsername.text.toString().trim()
                    lifecycleScope.launch{
                        rtUser.changeUsername(username)
                    }
                }
            }
        }
    }

    private fun setInfoFromSharedPrefs() {
        sharedPreferences =
            requireActivity().getSharedPreferences(
                settingsName, Context.MODE_PRIVATE
            )

        val themeColor = sharedPreferences?.getString(themePreferencesName, "").orEmpty()
        setThemeSelectedIcon(themeColor)

        val language = sharedPreferences?.getString(languagePreferencesName, "").orEmpty()
        setLangSelectedColor(language)
    }

    private fun setOnClickListenersForLangButtons() {
        binding?.apply {
            bEnglish.setOnClickListener {
                setLanguage("en")
            }
            bUkrainian.setOnClickListener {
                setLanguage("uk")
            }
            bRussian.setOnClickListener {
                setLanguage("ru")
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
                "en" -> setLangButtonColor(arrayListOf(bRussian, bUkrainian), bEnglish)
                "uk" -> setLangButtonColor(arrayListOf(bRussian, bEnglish), bUkrainian)
                "ru" -> setLangButtonColor(arrayListOf(bEnglish, bUkrainian), bRussian)
            }
        }
    }

    private fun setOnClickListenersForThemeButtons() {
        binding?.apply {
            bYellowTheme.setOnClickListener {
                setThemeColor("yellow")
            }

            bRedTheme.setOnClickListener {
                setThemeColor("red")
            }

            bGreenTheme.setOnClickListener {
                setThemeColor("green")
            }

            bPurpleTheme.setOnClickListener {
                setThemeColor("purple")
            }

            bBlueTheme.setOnClickListener {
                setThemeColor("blue")
            }
        }
    }

    private fun addTextChangeListeners() {
        binding?.apply {
            etUsername.addTextChangedListener {
                val text = etUsername.text.toString().trim()
                bChangeUsername.isActivated = text != username
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
                "yellow" -> setButtonIcon(buttonArray, bYellowTheme)
                "green" -> setButtonIcon(buttonArray, bGreenTheme)
                "blue" -> setButtonIcon(buttonArray, bBlueTheme)
                "purple" -> setButtonIcon(buttonArray, bPurpleTheme)
                "red" -> setButtonIcon(buttonArray, bRedTheme)
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

    private fun changeIsEnabledAllViews(enabled: Boolean) {
        binding?.apply {
            val buttonArray = arrayListOf(
                bAskQuestion, bEnglish,
                bUkrainian, bRussian,
                bPurpleTheme, bGreenTheme,
                bBlueTheme, bRedTheme,
                bYellowTheme, bBack,
                bLogo, bChangePhoto, bChangeUsername, etUsername
            )
            buttonArray.forEach {
                it.isEnabled = !enabled
            }
        }
    }
}