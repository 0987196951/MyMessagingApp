package com.example.mymessagingapp.Main.system.modelview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymessagingapp.Main.utils.CONSTANT
import com.example.mymessagingapp.Main.system.data.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class ListMemBerViewModelFactory(private val groupId : String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(String::class.java).newInstance(groupId)
    }
    var listMemberLiveData : MutableLiveData<MutableList<User>> = MutableLiveData(mutableListOf())
    init {
        Firebase.firestore.collection(CONSTANT.KEY_GROUP).document(groupId)
            .get().addOnSuccessListener { value ->
                val listUserId = value.data?.get(CONSTANT.KEY_GROUP_LIST_MEMBER) as List<String>
                for (s in listUserId){
                    Firebase.firestore.collection(CONSTANT.KEY_USER).document(s)
                        .get().addOnSuccessListener { value ->
                            listMemberLiveData.value?.add(User(value.data?.get(CONSTANT.KEY_USER_ID) as String,
                                    value.data!!.get(CONSTANT.KEY_USER_NAME) as String,
                                    "",
                                "",
                                    Date(),
                                    Date(),
                                    value.data!!.get(CONSTANT.KEY_USER_IMAGE) as String,
                                    false
                            ))
                            listMemberLiveData.notifyObserver()
                        }
                }
                listMemberLiveData.notifyObserver()
            }
    }
    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}