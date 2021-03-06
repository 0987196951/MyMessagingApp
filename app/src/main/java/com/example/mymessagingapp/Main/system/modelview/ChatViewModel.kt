package com.example.mymessagingapp.Main.system.modelview

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymessagingapp.Main.utils.CONSTANT
import com.example.mymessagingapp.Main.system.data.ChatMessage
import com.example.mymessagingapp.Main.system.data.Group
import com.example.mymessagingapp.Main.system.data.User
import com.example.mymessagingapp.Main.system.Fragment.utilities.Inites
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
private val TAG = "ChatViewModel"
class ChatViewModelFactory(val user: User, val group : Group) : ViewModelProvider.Factory{
    private val db = Firebase.firestore
    private var check : Boolean = false
    private var groupMapping : String? = null
    var listMessage : MutableLiveData<MutableList<ChatMessage>> = MutableLiveData(mutableListOf<ChatMessage>())
    init {
        /*db.collection(CONSTANT.KEY_GROUP)
            .document(group.groupId).collection(CONSTANT.KEY_MESSAGE)
            .get().addOnSuccessListener {  value ->
                for(doc in value){
                    val senderId = doc.data.get(CONSTANT.KEY_MESSAGE_SENDER_ID) as String
                    val message = doc.data.get(CONSTANT.KEY_MESSAGE_CONTENT) as String
                    val timeMessage = Inites.convertTimeStampToDate(doc.data[CONSTANT.KEY_MESSAGE_TIME_SEND] as Timestamp)
                    listMessage.value?.add(ChatMessage(senderId, message, timeMessage))
                }
                listMessage.notifyObserver()
            }*/
        if(!group.isGroup){
            db.collection(CONSTANT.KEY_GROUP).document(group.groupId).get()
                .addOnSuccessListener { value ->
                    if(value != null){
                        groupMapping = value.data?.get(CONSTANT.KEY_GROUP_MAPPING) as String
                    }
                }
        }
        listenerChatMessageChange()
    }
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(User::class.java, Group::class.java).newInstance(user, group)
    }
    fun listenerChatMessageChange(){
        val db = Firebase.firestore
        db.collection(CONSTANT.KEY_GROUP).document(group.groupId)
            .collection(CONSTANT.KEY_MESSAGE)
            .addSnapshotListener { value, e ->
                if(e != null) {
                    return@addSnapshotListener
                }
                if(value != null ){
                    for(doc in value.documentChanges){
                        if(doc.type == DocumentChange.Type.ADDED){
                            Log.d(TAG, "change message")
                            val senderName = doc.document.getString(CONSTANT.KEY_MESSAGE_SENDER_NAME) as String
                            val senderId = doc.document.getString(CONSTANT.KEY_MESSAGE_SENDER_ID) as String
                            val message = doc.document.getString(CONSTANT.KEY_MESSAGE_CONTENT) as String
                            val timeMessage = Inites.convertTimeStampToDate(doc.document[CONSTANT.KEY_MESSAGE_TIME_SEND] as Timestamp)
                            listMessage.value?.add(ChatMessage(senderId, message, timeMessage, senderName))
                            if(!check) {
                                continue
                            }
                            if(senderId != CONSTANT.KEY_MESSAGE_SYSTEM_ID){
                                db.collection(CONSTANT.KEY_GROUP).document(group.groupId)
                                    .update(mapOf(
                                        CONSTANT.KEY_CONVERSATION to mapOf(
                                            CONSTANT.KEY_CONVERSATION_SENDER_NAME to senderName,
                                            CONSTANT.KEY_CONVERSATION_CONTENT to message,
                                            CONSTANT.KEY_CONVERSATION_TIME_SEND to timeMessage
                                        )
                                    ))
                            }
                            if(!group.isGroup){
                                db.collection(CONSTANT.KEY_GROUP).document(groupMapping!!)
                                    .update(mapOf(
                                        CONSTANT.KEY_CONVERSATION to mapOf(
                                            CONSTANT.KEY_CONVERSATION_SENDER_NAME to senderName,
                                            CONSTANT.KEY_CONVERSATION_CONTENT to message,
                                            CONSTANT.KEY_CONVERSATION_TIME_SEND to timeMessage
                                        )
                                    ))
                            }
                        }
                    }
                    if(check == false){
                        Collections.sort(listMessage.value)
                    }
                    check = true
                    listMessage.notifyObserver()
                }
            }
    }
    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}