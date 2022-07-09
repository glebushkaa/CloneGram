package com.example.clonegramtestproject.ui.message.viewmodels

import androidx.lifecycle.ViewModel
import com.example.clonegramtestproject.data.firebase.realtime.RealtimeNewUser
import com.example.clonegramtestproject.data.models.CommonModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FindNewUserViewModel(
    private val rtNewUser: RealtimeNewUser
) : ViewModel() {

    val allUsersList = ArrayList<CommonModel>()
    val filteredUsersList = ArrayList<CommonModel>()
    val uidList = ArrayList<String>()

    fun getNewUsers() {
        var filterArrayUsers = ArrayList<CommonModel>()
        if (uidList.isEmpty()) {
            filterArrayUsers = allUsersList
        } else {
            allUsersList.forEach { user ->
                if (!uidList.contains(user.uid)) {
                    filterArrayUsers.add(user)
                }
            }
        }
        filteredUsersList.clear()
        filteredUsersList.addAll(filterArrayUsers)
    }

    suspend fun addUserToChat(user: CommonModel) = rtNewUser.addUserToChat(user)


    suspend fun filterUsersArray(filterText: String) =
        suspendCoroutine<ArrayList<CommonModel>> { emitter ->
           if(filterText.isNotEmpty()){
               val arrayList = ArrayList<CommonModel>()

               filteredUsersList.forEach {
                   if (it.phone?.startsWith("+$filterText") == true ||
                       it.phone?.startsWith(filterText) == true
                   ) {
                       arrayList.add(it)
                   }
               }
               emitter.resume(arrayList)
           }else{
               emitter.resume(filteredUsersList)
           }
        }
}