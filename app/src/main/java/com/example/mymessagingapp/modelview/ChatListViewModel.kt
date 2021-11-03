package com.example.mymessagingapp.modelview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymessagingapp.CONSTANT
import com.example.mymessagingapp.adapter.ConversationAdapter
import com.example.mymessagingapp.data.Conversation
import com.example.mymessagingapp.data.User
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*
private val TAG = "ChatListViewModel"
class ChatListViewModelFactory(val user : User,
                               private val context: Context,
                               private val layoutInflater: LayoutInflater  ) : ViewModelProvider.Factory {
    private val db = Firebase.firestore
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(User::class.java, Context::class.java, LayoutInflater::class.java)
            .newInstance(user, context, layoutInflater)
    }
    val conversationAdapter : MutableLiveData<ConversationAdapter>
        = MutableLiveData(ConversationAdapter(user,getConversationAndAddSnapShot(), context, layoutInflater))
    private fun getConversationAndAddSnapShot() :MutableList<Conversation>{
        var listGroupId : List<String> = emptyList()
        var listConversation : MutableList<Conversation> = mutableListOf()
        db.collection(CONSTANT.KEY_USER).document(user.userId)
            .get()
            .addOnSuccessListener { value ->
                if(value != null){
                    listGroupId.plus(value.toObject<User>()?.group_list_id)
                }
            }.addOnFailureListener{
                Log.d("TAG", "you can init list Group Container User")
                return@addOnFailureListener
            }
        db.collection(CONSTANT.KEY_GROUP).whereEqualTo(CONSTANT.KEY_USER_ID, user.userId)
            .addSnapshotListener{ snapShot,e ->
                if(e != null){
                    return@addSnapshotListener
                }
                if (snapShot != null) {
                    for(doc in snapShot.documentChanges){
                        if(doc.type == DocumentChange.Type.ADDED || doc.type == DocumentChange.Type.MODIFIED || doc.type == DocumentChange.Type.REMOVED){
                            listGroupId = doc.document.data.get(CONSTANT.KEY_GROUP_ID) as List<String>
                        }
                    }
                }
            }
        db.collection(CONSTANT.KEY_GROUP).whereIn(CONSTANT.KEY_GROUP_ID, listGroupId).get()
            .addOnSuccessListener { value ->
                if(value != null){
                    for(doc in value){
                        val senderId = doc.data.get("conversation.senderId") as String
                        val content = doc.data.get("conversation.content") as String
                        val timeSend = doc.data.get("conversation.timeSend") as Date
                        listConversation.add(Conversation(senderId, content, timeSend))
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Can't make List Conversation" )
                return@addOnFailureListener
            }
        db.collection(CONSTANT.KEY_GROUP)
            .whereIn(CONSTANT.KEY_GROUP_ID, listGroupId)
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if(e != null) {
                    return@EventListener
                }
                if(value != null) {
                    for(documentChange in value.documentChanges){
                        if(documentChange.type == DocumentChange.Type.ADDED || documentChange.type == DocumentChange.Type.MODIFIED){
                            val senderId : String = documentChange.document.getString("conversation.senderId")!!
                            val lastMes : String = documentChange.document.getString("conversation.lastMessage")!!
                            val timeLastMes : Date = documentChange.document.getDate("conversation.timeLastMessage")!!
                            conversationAdapter.value?.addConversation(Conversation(senderId, lastMes, timeLastMes))
                        }
                        else if(documentChange.type == DocumentChange.Type.REMOVED){
                            val senderId : String = documentChange.document.getString("conversation.senderId")!!
                            val lastMes : String = documentChange.document.getString("conversation.lastMessage")!!
                            val timeLastMes : Date = documentChange.document.getDate("conversation.timeLastMessage")!!
                            conversationAdapter.value?.removeConversation(Conversation(senderId, lastMes, timeLastMes))
                        }
                    }
                }
            })
        return listConversation
    }
}