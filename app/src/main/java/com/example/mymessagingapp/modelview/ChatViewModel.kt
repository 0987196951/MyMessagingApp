package com.example.mymessagingapp.modelview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymessagingapp.CONSTANT
import com.example.mymessagingapp.data.ChatMessage
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import java.util.*

class ChatViewModelFactory(val user: User, val group : Group) : ViewModelProvider.Factory{
    private val db = Firebase.firestore
    lateinit var listMessage : LiveData<List<ChatMessage>>
    init {
        db.collection(CONSTANT.KEY_MESSAGE)
            .document(group.groupId).collection(CONSTANT.KEY_MESSAGE)
            .get().addOnSuccessListener { value ->
                for(doc in value){
                    listMessage.value?.plus(doc.toObject<ChatMessage>())
                }
            }
    }
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(User::class.java, Group::class.java).newInstance(user, group)
    }
    fun addChatMessage(chat : ChatMessage){
        listMessage.value?.plus(chat)
    }
    private fun listenerChatMessageChange(){
        val db = Firebase.firestore
        db.collection(CONSTANT.KEY_GROUP).document(group.groupId)
            .collection(CONSTANT.KEY_MESSAGE)
            .addSnapshotListener( EventListener<QuerySnapshot> { value, e ->
                if(e != null) {
                    return@EventListener
                }
                if(value != null){
                    for(doc in value.documentChanges){
                        if(doc.type == DocumentChange.Type.ADDED){
                            val senderId = doc.document.getString("senderId")
                            val message = doc.document.getString("message")
                            val timeMessage = doc.document.getDate("timeMessage")
                            listMessage.value?.plus(ChatMessage(senderId!!, message!!, timeMessage!!))
                                db.collection(CONSTANT.KEY_GROUP).document(group.groupId)
                                    .update(mapOf(
                                        "conversation.senderId" to senderId,
                                        "conversation.message" to message,
                                        "conversation.timeMessage" to timeMessage
                                    ))
                        }
                    }
                }
            })
    }
}