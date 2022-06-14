package com.example.clonegramtestproject.ui.login.fragments

import android.os.Bundle
import android.view.Display
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.Animations
import com.example.clonegramtestproject.FlagsEmojies
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CountriesCodes
import com.example.clonegramtestproject.databinding.FragmentCountryBinding
import com.example.clonegramtestproject.ui.login.recylerview.CountryAdapter
import com.example.clonegramtestproject.ui.login.viewmodels.CountryViewModel
import com.example.clonegramtestproject.utils.CHOSEN_COUNTRY
import com.example.clonegramtestproject.utils.closeSoftKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CountryFragment : Fragment(R.layout.fragment_country) {

    private val viewModel by viewModels<CountryViewModel>()
    private var binding: FragmentCountryBinding? = null
    private var adapter: CountryAdapter? = null

    private val animations = Animations()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentCountryBinding.bind(view)
        viewModel.let { vm ->
            lifecycleScope.launch(Dispatchers.IO){
                arguments?.let {
                    vm.chosenCountry = it.getString("chosenCountry").orEmpty()
                    vm.countryArrayList.addAll(
                        it.getParcelableArrayList("countryCodeArray")!!
                    )
                }
                vm.filteredArrayList.addAll(vm.countryArrayList)
            }
        }
        initAdapter()
        setOnClickListeners()
        setOnQueryTextListener()
        changeSearchViewTextColor()
    }

    private fun setOnClickListeners() {
        binding?.apply {

            bSearch.setOnClickListener {
                animations.openSearchView(bSearch, searchView, bBack)
                searchView.isIconified = false
            }

            searchView.setOnCloseListener {
                animations.closeSearchView(bSearch, searchView, bLogo)
                false
            }

            bBack.setOnClickListener {
                findNavController().navigate(
                    R.id.country_to_login
                )
            }

            adapter?.setOnItemClickListener(object : CountryAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val countryCode = viewModel.
                    filteredArrayList[position].dial_code.trim()
                    viewModel.chosenCountry = countryCode
                        .substring(1, countryCode.length)
                    findNavController().navigate(
                        R.id.country_to_login,
                        bundleOf(
                            CHOSEN_COUNTRY to viewModel.chosenCountry
                        )
                    )
                }
            })
        }
    }

    private fun setOnQueryTextListener() {
        binding!!.searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    closeSoftKeyboard(
                        binding!!.searchView, requireActivity()
                    )
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    lifecycleScope.launch(Dispatchers.IO){
                        viewModel.fillFilterList(newText)
                    }
                    adapter?.refreshData(viewModel.filteredArrayList)
                    return false
                }

            }
        )
    }

    private fun changeSearchViewTextColor(){
        val editText : EditText = binding!!.searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        editText.background = null
        editText.setTextColor(resources.getColor(R.color.white,null))
    }

    private fun initAdapter() {
        adapter = CountryAdapter(FlagsEmojies.countriesFlags)
        binding!!.rvCountries.adapter = adapter
        adapter!!.refreshData(viewModel.filteredArrayList)

    }
}