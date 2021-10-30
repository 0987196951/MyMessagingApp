package com.example.mymessagingapp.modelview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymessagingapp.CONSTANT
import com.example.mymessagingapp.data.ChatMessage
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase

class ChatViewModelFactory(val user: User, val group : Group) : ViewModelProvider.Factory{
    private val db = Firebase.firestore
    lateinit var listMessage : LiveData<List<ChatMessage>>
    init {
        db.collection(CONSTANT.KEY_MESSAGE)
            .whereEqualTo("groupId" , group.groupId)
            .get().addOnSuccessListener { value ->
                for(doc in value){
                    listMessage.value?.plus(doc.toObject<ChatMessage>())
                }
            }
    }
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(User::class.java, Group::class.java).newInstance(user, group)
    }
}