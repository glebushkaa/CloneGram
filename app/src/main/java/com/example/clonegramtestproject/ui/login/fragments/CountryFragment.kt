package com.example.clonegramtestproject.ui.login.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.Animations
import com.example.clonegramtestproject.FlagsEmojies
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CountriesCodes
import com.example.clonegramtestproject.databinding.FragmentCountryBinding
import com.example.clonegramtestproject.ui.login.recylerview.CountryAdapter
import com.example.clonegramtestproject.utils.CHOSEN_COUNTRY
import com.example.clonegramtestproject.utils.closeSoftKeyboard


class CountryFragment : Fragment(R.layout.fragment_country) {

    private var binding: FragmentCountryBinding? = null

    private var adapter: CountryAdapter? = null

    private var chosenCountry = ""

    private val countryArrayList = arrayListOf<CountriesCodes>()
    private val filteredArrayList = arrayListOf<CountriesCodes>()

    private val animations = Animations()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentCountryBinding.bind(view)

        val editText : EditText = binding!!.searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        editText.background = null
        editText.setTextColor(resources.getColor(R.color.white,null))

        arguments?.let {
            chosenCountry = it.getString("chosenCountry").orEmpty()
            countryArrayList.addAll(it.getParcelableArrayList("countryCodeArray")!!)
        }
        filteredArrayList.addAll(countryArrayList)

        initAdapter()
        setOnClickListeners()
        setOnQueryTextListener()
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
                    val countryCode = filteredArrayList[position].dial_code.trim()
                    chosenCountry = countryCode
                        .substring(1, countryCode.length)
                    findNavController().navigate(
                        R.id.country_to_login,
                        bundleOf(
                            CHOSEN_COUNTRY to chosenCountry
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
                    filteredArrayList.clear()
                    countryArrayList.forEach {
                        if (it.name.lowercase().contains(newText.orEmpty().lowercase())) {
                            filteredArrayList.add(it)
                        }
                    }
                    adapter?.refreshData(filteredArrayList)
                    return false
                }

            }
        )
    }

    private fun initAdapter() {
        adapter = CountryAdapter(FlagsEmojies.countriesFlags)
        adapter!!.refreshData(filteredArrayList)
        binding!!.rvCountries.adapter = adapter
    }
}