package com.example.mymessagingapp.modelview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymessagingapp.CONSTANT
import com.example.mymessagingapp.data.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class FindOtherUserViewModel(private val key_find : String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(String::class.java).newInstance(key_find)
    }
    private lateinit var listOtherUser : MutableList<User>
    init {
        Firebase.firestore.collection(CONSTANT.KEY_USER)
            .whereGreaterThan("name", key_find)
            .whereLessThanOrEqualTo("name", key_find + '\uf8ff')
            .get().addOnSuccessListener { values ->
                for(doc in values){
                    listOtherUser.plus(values.toObjects<User>())
                }
            }
    }
    fun getListOtherUser() : MutableList<User>{
        return listOtherUser
    }
}