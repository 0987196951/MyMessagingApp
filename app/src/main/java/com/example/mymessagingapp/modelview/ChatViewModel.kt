package com.example.mymessagingapp.modelview

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymessagingapp.CONSTANT
import com.example.mymessagingapp.data.ChatMessage
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.utilities.Inites
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import java.util.*
private val TAG = "ChatViewModel"
class ChatViewModelFactory(val user: User, val group : Group) : ViewModelProvider.Factory{
    private val db = Firebase.firestore
    var listMessage : MutableLiveData<MutableList<ChatMessage>> = MutableLiveData(mutableListOf<ChatMessage>())
    init {
        db.collection(CONSTANT.KEY_GROUP)
            .document(group.groupId).collection(CONSTANT.KEY_MESSAGE)
            .get().addOnSuccessListener { value ->
                for(doc in value){
                    val senderId = doc.data.get(CONSTANT.KEY_MESSAGE_SENDER_ID) as String
                    val message = doc.data.get(CONSTANT.KEY_MESSAGE_CONTENT) as String
                    val timeMessage = Inites.convertTimeStampToDate(doc.data[CONSTANT.KEY_MESSAGE_TIME_SEND] as Timestamp)
                    listMessage.value?.plus(ChatMessage(senderId, message, timeMessage))
                }
                //listMessage.notifyObserver()
            }.continueWith {
                listenerChatMessageChange()
            }
    }
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(User::class.java, Group::class.java).newInstance(user, group)
    }
    fun listenerChatMessageChange(){
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
                            val senderId = doc.document.getString(CONSTANT.KEY_MESSAGE_SENDER_ID) as String
                            val message = doc.document.getString(CONSTANT.KEY_MESSAGE_CONTENT) as String
                            val timeMessage = Inites.convertTimeStampToDate(doc.document[CONSTANT.KEY_MESSAGE_TIME_SEND] as Timestamp)
                            listMessage.value?.add(ChatMessage(senderId, message, timeMessage)).let {
                                listMessage.notifyObserver()
                            }
                                db.collection(CONSTANT.KEY_GROUP).document(group.groupId)
                                    .update(mapOf(
                                         CONSTANT.KEY_CONVERSATION to mapOf(
                                             CONSTANT.KEY_CONVERSATION_SENDER_NAME to senderId,
                                             CONSTANT.KEY_CONVERSATION_CONTENT to message,
                                             CONSTANT.KEY_CONVERSATION_TIME_SEND to timeMessage
                                         )
                                    ))
                        }
                    }
                }
            })
    }
    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}