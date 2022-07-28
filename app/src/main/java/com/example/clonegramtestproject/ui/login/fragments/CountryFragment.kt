package com.example.clonegramtestproject.ui.login.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.Flags
import com.example.clonegramtestproject.databinding.FragmentCountryBinding
import com.example.clonegramtestproject.ui.Animations
import com.example.clonegramtestproject.ui.login.recylerview.CountryAdapter
import com.example.clonegramtestproject.ui.login.viewmodels.CountryViewModel
import com.example.clonegramtestproject.utils.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class CountryFragment : Fragment(R.layout.fragment_country) {

    private val viewModel by viewModel<CountryViewModel>()
    private var binding: FragmentCountryBinding? = null
    private var adapter: CountryAdapter? = null

    private val animations: Animations by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().changeStatusBarColor(requireContext().getColorAppColor())
        binding = FragmentCountryBinding.bind(view)
        getArgs()
        setOnClickListeners()
        setOnQueryTextListener()
        changeSearchViewTextColor()
        initAdapter()
    }

    private fun setOnClickListeners() {
        binding?.apply {

            bSearch.setOnClickListener {
                animations.openSearchView(bSearch, searchView, bBack)
                searchView.isIconified = false
            }

            searchView.setOnCloseListener {
                animations.closeSearchView(bSearch, searchView,toolbarCard)
                false
            }

            bBack.setOnClickListener {
                findNavController().navigate(
                    R.id.country_to_login
                )
            }
        }
    }

    private fun setOnQueryTextListener() {
        binding?.searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    requireActivity().closeSoftKeyboard(binding?.searchView!!)
                    return false
                }

                override fun onQueryTextChange(text: String?): Boolean {
                    lifecycleScope.launch {
                        val a = async {
                            viewModel.filterList(text)
                        }
                        adapter?.refreshData(a.await())
                    }
                    return false
                }

            }
        )
    }

    private fun changeSearchViewTextColor() {
        val editText: EditText? =
            binding?.searchView?.findViewById(androidx.appcompat.R.id.search_src_text)
        editText?.background = null
        editText?.setTextColor(resources.getColor(R.color.white, null))
    }

    private fun initAdapter() {
        adapter = CountryAdapter(Flags.countriesFlags) {
            findNavController().navigate(
                R.id.country_to_login,
                bundleOf(
                    "chosenCountry" to viewModel.getCountryCode(it)
                )
            )
        }
        binding?.rvCountries?.adapter = adapter
        adapter?.refreshData(viewModel.filteredList)
    }

    private fun getArgs() {
        arguments?.let {
            viewModel.setVariables(
                it.getParcelableArrayList(
                    COUNTRY_CODE_ARR
                ),
                it.getString(CHOSEN_COUNTRY)
            )
        }
    }
}