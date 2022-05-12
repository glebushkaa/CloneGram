package com.example.clonegramtestproject.ui.message.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.clonegramtestproject.data.CommonModel
import com.example.clonegramtestproject.utils.USERS_MESSAGES_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class GeneralMessageViewModel : ViewModel() {
    private val firebaseDatabase = Firebase.database
    private val databaseRefMessages = firebaseDatabase.getReference(USERS_MESSAGES_NODE)
    private val currentUID = FirebaseAuth.getInstance().currentUser?.uid

    private var allMessagedUsersListener: ValueEventListener? = null

    val allMessagedUsersLiveData = MutableLiveData<List<CommonModel>>()

    fun addAllMessagedUsersListener() {
        val allMessagedUsersList = ArrayList<CommonModel>()

        allMessagedUsersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    allMessagedUsersList.clear()
                    for (item in snapshot.children) {
                        item.getValue(CommonModel::class.java)
                            ?.let {
                                if (it.permissionUidArray?.get(currentUID) == true) {
                                    allMessagedUsersList.add(it)
                                }
                            }
                    }
                    allMessagedUsersLiveData.value = allMessagedUsersList
                } else {
                    allMessagedUsersLiveData.value = arrayListOf()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }.let {
            databaseRefMessages.addValueEventListener(it)
        }
    }

    fun removeAllMessagedListener() {
        allMessagedUsersListener?.let {
            databaseRefMessages.removeEventListener(it)
        }
    }

}