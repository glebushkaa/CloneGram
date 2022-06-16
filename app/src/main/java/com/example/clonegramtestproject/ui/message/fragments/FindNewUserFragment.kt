package com.example.clonegramtestproject.ui.message.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.Animations
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.models.CommonModel
import com.example.clonegramtestproject.databinding.FragmentFindNewUserBinding
import com.example.clonegramtestproject.ui.message.recyclerview.newuser.NewUserAdapter
import com.example.clonegramtestproject.ui.message.viewmodels.FindNewUserViewModel
import com.example.clonegramtestproject.utils.ALL_USER_LIST
import com.example.clonegramtestproject.utils.UID_LIST
import com.example.clonegramtestproject.utils.USER
import com.example.clonegramtestproject.utils.showToast
import kotlinx.coroutines.launch


class FindNewUserFragment : Fragment(R.layout.fragment_find_new_user) {

    private val viewModel by viewModels<FindNewUserViewModel>()
    private var binding: FragmentFindNewUserBinding? = null
    private var adapter: NewUserAdapter? = null

    private val animations = Animations()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentFindNewUserBinding.bind(view)

        changeSearchViewColor()
        getArgs()
        viewModel.getNewUsers()
        initAdapter()
        setOnClickListeners()
        addOnQueryTextListener()
    }


    private fun changeSearchViewColor() {
        val editText: EditText? =
            binding?.searchView?.findViewById(androidx.appcompat.R.id.search_src_text)
        editText?.background = null
        editText?.setTextColor(resources.getColor(R.color.white, null))

    }

    private fun getArgs() {
        viewModel.let { vm ->
            arguments?.let {
                it.getStringArrayList(UID_LIST)?.let { list ->
                    vm.uidList.clear()
                    vm.uidList.addAll(list)
                }

                it.getParcelableArrayList<CommonModel>(ALL_USER_LIST)
                    ?.let { list ->
                        vm.allUsersList.clear()
                        vm.allUsersList.addAll(list)
                    }
            }
        }
    }

    private fun initAdapter() {
        adapter = NewUserAdapter { user ->
            lifecycleScope.launch {
                viewModel.addUserToChat(user)?.let { chatUID ->
                    findNavController().navigate(
                        R.id.find_user_to_direct,
                        bundleOf(
                            USER to CommonModel(
                                username = user.username,
                                phone = user.phone,
                                chatUID = chatUID,
                                uid = user.uid,
                                userPicture = user.userPicture
                            ),
                        )
                    )
                }
            }
        }
        binding?.recyclerView?.adapter = adapter
        adapter?.setData(viewModel.filteredUsersList)
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
                findNavController().popBackStack()
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
                    override fun onQueryTextChange(newText: String?): Boolean {
                        lifecycleScope.launch {
                            viewModel.filterUsersArray(newText.orEmpty()).let { list ->
                                adapter?.setData(list)
                            }
                        }
                        return false
                    }

                }
            )
        }
    }
}