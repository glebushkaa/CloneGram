package com.example.clonegramtestproject.ui.message.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.clonegramtestproject.Animations
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.databinding.FragmentGeneralMessageBinding
import com.example.clonegramtestproject.firebase.realtime.RealtimeGetter
import com.example.clonegramtestproject.ui.message.recyclerview.general.GeneralAdapter
import com.example.clonegramtestproject.ui.message.viewmodels.GeneralMessageViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class   GeneralMessageFragment : Fragment(R.layout.fragment_general_message) {

    private val viewModel by viewModels<GeneralMessageViewModel>()

    private var adapter: GeneralAdapter? = null

    private var binding: FragmentGeneralMessageBinding? = null

    private var username: String? = null
    private var user: CommonModel? = null

    private var auth = FirebaseAuth.getInstance()
    private var currentUID = auth.currentUser?.uid.orEmpty()
    private var phoneNumber = auth.currentUser?.phoneNumber


    private var uidList = ArrayList<String?>()

    private var allUsersList = ArrayList<CommonModel>()
    private val messagesList = ArrayList<CommonModel>()
    private var visibleDataList = ArrayList<CommonModel>()
    private var filteredUsersList = ArrayList<CommonModel>()

    private val firebaseGetter = RealtimeGetter()
    private val animations = Animations()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentGeneralMessageBinding.bind(view)

        val editText : EditText = binding!!.searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        editText.background = null
        editText.setTextColor(resources.getColor(R.color.white,null))

        setOnClickListeners()
        setDrawerNavigation()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            initAdapter()
            with((Dispatchers.IO)) {
                user = firebaseGetter.getUser(currentUID)
                username = user?.username
                allUsersList = firebaseGetter.getAllUsersList()
                viewModel.addAllMessagedUsersListener()

                addObserverListeners()
                addOnQueryTextListener()
            }
            setTextForViews()
        }
    }

    private fun addObserverListeners() {
        viewModel.allMessagedUsersLiveData.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                with(Dispatchers.IO) {
                    messagesList.clear()
                    messagesList.addAll(it)
                    uidList = getUidList()
                    visibleDataList = sortVisibleGeneralData()
                }
                visibleDataList.let { list->
                    list.sortBy {
                        it.lastMessage?.get(currentUID)?.timestamp
                    }
                    list.reverse()
                    adapter?.setData(list)
                }
            }
        }
    }

    private fun initAdapter() {
        binding?.apply {
            adapter = GeneralAdapter(requireContext(), object : GeneralAdapter.OnItemClickListener {
                override fun onItemClicked(user: CommonModel) {
                    lifecycleScope.launch {
                        findNavController().navigate(
                            R.id.general_to_direct,
                            bundleOf(
                                "user" to user
                            )
                        )
                    }
                }
            })
            rvGeneral.adapter = adapter
            rvGeneral.itemAnimator = null
        }
    }

    private fun setTextForViews() {
        val headerView = binding?.navMenu?.getHeaderView(0)
        val userPicture = headerView?.findViewById<ImageView>(R.id.userPicture)
        user?.userPicture?.let {
            if (userPicture != null) {
                Glide.with(requireContext())
                    .load(it)
                    .circleCrop()
                    .into(userPicture)
            }
        }
        headerView?.findViewById<TextView>(R.id.tvUsername)?.text = username
        headerView?.findViewById<TextView>(R.id.tvPhone)?.text = phoneNumber
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
                                "user" to user
                            )
                        )
                    }

                    R.id.findNewUser -> {
                        drawer.closeDrawer(GravityCompat.START)
                        findNavController().navigate(
                            R.id.general_to_find_user,
                            bundleOf(
                                "allUsersList" to allUsersList,
                                "uidList" to uidList
                            )
                        )
                    }
                }
                true
            }
        }
    }

    private suspend fun getUidList() = suspendCoroutine<ArrayList<String?>> { emitter ->

        val listOfUID = ArrayList<String?>()

        messagesList.forEach {
            it.uidArray?.forEach { uid ->
                if (uid != auth.currentUser?.uid) {
                    listOfUID.add(uid)
                }
            }
        }
        emitter.resume(listOfUID)
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
                                    filteredUsersList.clear()
                                    filteredUsersList
                                        .addAll(filterUsersArray(newText.orEmpty()))
                                }
                                adapter?.setData(filteredUsersList)
                            }
                        } else if (newText.orEmpty().isEmpty()) {
                            adapter?.setData(visibleDataList)
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

            visibleDataList.forEach {
                if (it.username?.contains(filterText) == true &&
                    it.uid != currentUID
                ) {
                    arrayList.add(it)
                }
            }
            emitter.resume(arrayList)
        }


    private suspend fun sortVisibleGeneralData() =
        suspendCoroutine<ArrayList<CommonModel>> { emitter ->
            val generalVisibleData = ArrayList<CommonModel>()

            allUsersList.forEach {
                uidList.forEach { uid ->
                    if (it.uid == uid) {
                        val messageItem = messagesList[uidList.indexOf(uid)]
                        generalVisibleData.add(
                            CommonModel(
                                username = it.username,
                                phone = it.phone,
                                lastMessage = messageItem
                                    .lastMessage,
                                chatUID = messageItem
                                    .chatUID,
                                uid = it.uid,
                                userPicture = it.userPicture
                            )
                        )
                    }
                }
            }
            emitter.resume(generalVisibleData)
        }


    override fun onStop() {
        super.onStop()
        viewModel.removeAllMessagedListener()
    }

}