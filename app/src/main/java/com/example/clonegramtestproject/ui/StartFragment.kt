package com.example.clonegramtestproject.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.clonegramtestproject.R
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.firebase.realtime.RealtimeGetter
import com.example.clonegramtestproject.utils.USERS_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class StartFragment : Fragment(R.layout.fragment_start) {

    private val firebaseDatabase = Firebase.database
    private val auth = FirebaseAuth.getInstance()

    private val rtGetter = RealtimeGetter()

    private var user: CommonModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkIsUserExist()
    }

    private fun checkIsUserExist() {
        auth.currentUser?.let {
            lifecycleScope.launch {
                with(Dispatchers.IO)
                {
                    user = rtGetter
                        .getUser(auth.uid.orEmpty())
                }
                checkHasUserName()
            }
        } ?: run {
            findNavController().navigate(R.id.start_to_login)
        }
    }


    private fun checkHasUserName() {
        user?.username?.let {
            lifecycleScope.launch {
                findNavController().navigate(
                    R.id.start_to_message, bundleOf(
                        "user" to user
                    )
                )
            }
        } ?: run {
            firebaseDatabase.reference
                .child(USERS_NODE)
                .child(auth.currentUser?.phoneNumber.orEmpty()).removeValue()
            auth.currentUser?.delete()
            auth.signOut()
            findNavController().navigate(R.id.start_to_login)
        }
    }

}