package com.example.clonegramtestproject.ui.message.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.Animations
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.databinding.FragmentFindNewUserBinding
import com.example.clonegramtestproject.firebase.realtime.RealtimeNewUser
import com.example.clonegramtestproject.ui.message.recyclerview.newuser.NewUserAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class FindNewUserFragment : Fragment(R.layout.fragment_find_new_user) {

    private var binding: FragmentFindNewUserBinding? = null

    private var adapter : NewUserAdapter? = null

    private var allUsersList = ArrayList<CommonModel>()
    private var filteredUsersList = ArrayList<CommonModel>()
    private var changedFilteredList = ArrayList<CommonModel>()
    private var messagesList = ArrayList<String>()

    private var rtNewUser = RealtimeNewUser()

    private val animations = Animations()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentFindNewUserBinding.bind(view)

        val editText : EditText = binding!!.searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        editText.background = null
        editText.setTextColor(resources.getColor(R.color.white,null))

        arguments?.let {
            it.getStringArrayList("uidList")?.let { list ->
                messagesList = list
            }

            it.getParcelableArrayList<CommonModel>("allUsersList")
                ?.let { list ->
                    allUsersList = list
                }
        }

        lifecycleScope.launch {
            filteredUsersList = getNewUsers()
            initAdapter()
        }
        setOnClickListeners()
        addOnQueryTextListener()
    }

    private suspend fun getNewUsers() =
        suspendCoroutine<ArrayList<CommonModel>> {
            var filterArrayUsers = ArrayList<CommonModel>()
            if (messagesList.isEmpty()) {
                filterArrayUsers = allUsersList
            } else {
                allUsersList.forEach { user ->
                    if(!messagesList.contains(user.uid)){
                        filterArrayUsers.add(user)
                    }
                }
            }
            it.resume(filterArrayUsers)
        }

    private fun initAdapter() {
        adapter = NewUserAdapter(object : NewUserAdapter.OnItemClickListener {
            override fun onItemClicked(user: CommonModel) {
                var messageUID : String?
                lifecycleScope.launch {
                    with(Dispatchers.IO) {
                        messageUID = rtNewUser.addUserToMessages(user)
                    }
                    findNavController().navigate(
                        R.id.find_user_to_direct,
                        bundleOf(
                            "user" to CommonModel(
                                username = user.username,
                                phone = user.phone,
                                chatUID = messageUID,
                                uid = user.uid,
                                userPicture = user.userPicture
                            ),
                        )
                    )
                }
            }

        })
        binding?.recyclerView?.adapter = adapter
        adapter?.setData(filteredUsersList)
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
                        if (newText?.isNotEmpty() == true) {
                            lifecycleScope.launch {
                                with(Dispatchers.IO) {
                                    changedFilteredList.clear()
                                    changedFilteredList
                                        .addAll(filterUsersArray(newText.orEmpty()))
                                }
                                adapter?.setData(changedFilteredList)
                            }
                        } else if (newText.orEmpty().isEmpty()) {
                            adapter?.setData(filteredUsersList)
                        }
                        return false
                    }

                }
            )
        }
    }

    private suspend fun filterUsersArray(filterText: String) =
        suspendCoroutine<ArrayList<CommonModel>> { emitter ->
            val arrayList = ArrayList<CommonModel>()

            filteredUsersList.forEach {
                if (it.phone?.startsWith("+$filterText") == true ||
                    it.phone?.startsWith(filterText) == true
                ) {
                    arrayList.add(it)
                }
            }
            emitter.resume(arrayList)
        }


}