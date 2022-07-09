package com.example.clonegramtestproject.ui.message.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.ui.Animations
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.databinding.FragmentGeneralMessageBinding
import com.example.clonegramtestproject.ui.message.recyclerview.general.GeneralAdapter
import com.example.clonegramtestproject.ui.message.viewmodels.GeneralMessageViewModel
import com.example.clonegramtestproject.utils.ALL_USER_LIST
import com.example.clonegramtestproject.utils.UID_LIST
import com.example.clonegramtestproject.utils.USER
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class GeneralMessageFragment : Fragment(R.layout.fragment_general_message) {

    private val viewModel by viewModel<GeneralMessageViewModel>()

    private var adapter: GeneralAdapter? = null
    private var binding: FragmentGeneralMessageBinding? = null
    private val animations : Animations by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentGeneralMessageBinding.bind(view)
        changeSearchViewColor()
        setOnClickListeners()
        setDrawerNavigation()
        initAdapter()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            viewModel.setUser()
            viewModel.getAllUsersList()
            viewModel.getMessagedUsersListener()
            addObserverListeners()
            addOnQueryTextListener()
            setTextForViews()
        }
    }

    private fun addObserverListeners() {
        viewModel.allMessagedUsersLiveData.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                viewModel.sortData(it as ArrayList<CommonModel>).let { list ->
                    adapter?.setData(list)
                }
            }
        }
    }

    private fun initAdapter() {
        binding?.apply {
            adapter = GeneralAdapter(viewModel.currentUID.orEmpty())
            adapter?.setOnItemClickedListener(object : GeneralAdapter.OnItemClickListener {
                override fun onItemClicked(user: CommonModel) {
                    lifecycleScope.launch {
                        findNavController().navigate(
                            R.id.general_to_direct,
                            bundleOf(
                                USER to user
                            )
                        )
                    }
                }

                override fun deleteMyChatListener(chatUID: String) {
                    viewModel.deleteMyChat(chatUID)
                }

                override fun deleteChatListener(chatUID: String) {
                    viewModel.deleteChat(chatUID)
                }
            })
            rvGeneral.adapter = adapter
            rvGeneral.itemAnimator = null
        }
    }

    private fun changeSearchViewColor() {
        val editText: EditText = binding!!.searchView
            .findViewById(R.id.search_src_text)
        editText.background = null
        editText.setTextColor(resources.getColor(R.color.white, null))
    }

    private fun setTextForViews() {
        val headerView = binding?.navMenu?.getHeaderView(0)
        val userPicture = headerView?.findViewById<ImageView>(R.id.userPicture)
        viewModel.user?.userPicture?.let {
            if (userPicture != null) {
                Glide.with(requireContext()).load(it).circleCrop().into(userPicture)
            }
        }
        headerView?.findViewById<TextView>(R.id.tvUsername)?.text = viewModel.user?.username
        headerView?.findViewById<TextView>(R.id.tvPhone)?.text = viewModel.phoneNumber
    }

    private fun setOnClickListeners() {
        binding?.apply {

            bDrawerOpener.setOnClickListener {
                drawer.openDrawer(GravityCompat.START)
            }

            bSearch.setOnClickListener {
                animations.openSearchView(bSearch, searchView, bDrawerOpener)
                searchView.isIconified = false
            }

            searchView.setOnCloseListener {
                animations.closeSearchView(
                    bSearch = bSearch,
                    searchView = searchView,
                    edgeElement = bLogo
                )
                false
            }
        }
    }

    private fun setDrawerNavigation() {
        binding?.apply {

            navMenu.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.settings_item -> {
                        drawer.closeDrawer(GravityCompat.START)
                        findNavController().navigate(
                            R.id.general_to_settings,
                            bundleOf(
                                USER to viewModel.user
                            )
                        )
                    }

                    R.id.findNewUser -> {
                        drawer.closeDrawer(GravityCompat.START)
                        findNavController().navigate(
                            R.id.general_to_find_user,
                            bundleOf(
                                ALL_USER_LIST to viewModel.allUsersList,
                                UID_LIST to viewModel.uidList
                            )
                        )
                    }
                }
                true
            }
        }
    }

    private fun addOnQueryTextListener() {
        binding?.apply {
            searchView.setOnQueryTextListener(
                object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }
                    override fun onQueryTextChange(text: String?): Boolean {
                        lifecycleScope.launch {
                            viewModel.filterText(text?.lowercase()).let {
                                adapter?.setData(it)
                            }
                        }
                        return false
                    }

                }
            )
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.removeMessagedUsersListener()
    }

}