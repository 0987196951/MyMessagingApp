package com.example.mymessagingapp.Main.system.modelview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymessagingapp.Main.utils.CONSTANT
import com.example.mymessagingapp.Main.system.data.User
import com.example.mymessagingapp.Main.system.Fragment.utilities.Inites
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class FindOtherUserViewModel(private val key_find : String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(String::class.java).newInstance(key_find)
    }
    var listOtherUser : MutableLiveData<MutableList<User>> = MutableLiveData(mutableListOf())
    init {
        Firebase.firestore.collection(CONSTANT.KEY_USER)
            .whereEqualTo(CONSTANT.KEY_USER_NAME, key_find)
            //.whereGreaterThanOrEqualTo(CONSTANT.KEY_USER_NAME, key_find + '\uf8ff')
            .get().addOnSuccessListener { values ->
                if(values != null){
                    for(doc in values){
                        listOtherUser.value?.add(User(doc.data[CONSTANT.KEY_USER_ID] as  String,
                            doc.data[CONSTANT.KEY_USER_NAME] as  String,
                            doc.data[CONSTANT.KEY_USER_PASSWORD] as String,
                            doc.data[CONSTANT.KEY_USER_GMAIL] as String,
                            Inites.convertTimeStampToDate(doc.data[CONSTANT.KEY_USER_DATE_OF_BIRTH] as Timestamp),
                            Inites.convertTimeStampToDate(doc.data[CONSTANT.KEY_USER_CREATE_ACCOUNT] as Timestamp),
                            doc.data[CONSTANT.KEY_USER_IMAGE] as String,
                            false
                        ))
                    }
                }
                listOtherUser.notifyObserver()
            }
    }
    fun remakeListOtherUser(key_find : String){
        listOtherUser.value?.clear()
        Firebase.firestore.collection(CONSTANT.KEY_USER)
            .whereEqualTo(CONSTANT.KEY_USER_NAME, key_find)
            //.whereGreaterThanOrEqualTo(CONSTANT.KEY_USER_NAME, key_find + '\uf8ff')
            .get().addOnSuccessListener { values ->
                if(values != null){
                    for(doc in values){
                        listOtherUser.value?.add(User(doc.data[CONSTANT.KEY_USER_ID] as  String,
                            doc.data[CONSTANT.KEY_USER_NAME] as  String,
                            "",
                            "",
                            Date(),
                            Date(),
                            doc.data[CONSTANT.KEY_USER_IMAGE] as String,
                            false
                        ))
                    }
                }
                listOtherUser.notifyObserver()
            }
    }
    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}