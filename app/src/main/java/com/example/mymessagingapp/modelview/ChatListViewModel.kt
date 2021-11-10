package com.example.mymessagingapp.modelview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymessagingapp.CONSTANT
import com.example.mymessagingapp.adapter.ConversationAdapter
import com.example.mymessagingapp.data.Conversation
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.utilities.Inites
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
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
        var listGroupForward : List<String> = emptyList()
        var listConversation : MutableList<Conversation> = mutableListOf()
        db.collection(CONSTANT.KEY_USER).document(user.userId)
            .get()
            .addOnSuccessListener { value ->
                if(value != null){
                    listGroupId.plus(value.data?.get(CONSTANT.KEY_USER_LIST_GROUP_ID) as List<String>)
                }
            }.addOnFailureListener{
                Log.d("TAG", "you can init list Group Container User")
                return@addOnFailureListener
            }
        db.collection(CONSTANT.KEY_USER).document(user.userId)
            .addSnapshotListener{ snapShot,e ->
                if(e != null){
                    return@addSnapshotListener
                }
                if (snapShot != null && snapShot.exists()) {
                            listGroupId = snapShot.data?.get(CONSTANT.KEY_USER_LIST_GROUP_ID) as List<String>
                            if(listGroupForward.size == listGroupId.size){
                                Log.d(TAG, "==" + listGroupId)
                                return@addSnapshotListener
                            }
                            else if(listGroupForward.size < listGroupId.size) {
                                Log.d(TAG, "" + listGroupId)
                                val listIn1 = listGroupId.minus(listGroupForward)
                                for(s in listIn1){
                                    db.collection(CONSTANT.KEY_GROUP).document(s).get()
                                        .addOnSuccessListener { value ->
                                            conversationAdapter.value?.addConversation(getConversation(value, s))
                                            conversationAdapter.notifyObserver()
                                        }
                                }
                            }
                            else {
                                Log.d(TAG, "" + listGroupId)
                                val listIn2 = listGroupForward.minus(listGroupId)
                                for(s in listIn2){
                                    db.collection(CONSTANT.KEY_GROUP).document(s).get()
                                        .addOnSuccessListener { value ->
                                            conversationAdapter.value?.removeConversation(getConversation(value, s))
                                            conversationAdapter.notifyObserver()
                                        }
                                }
                            }
                            listGroupForward = listGroupId
                        }
            }
        for(s in listGroupId){
            db.collection(CONSTANT.KEY_GROUP).document(s).get()
                .addOnSuccessListener { value ->
                    listConversation.add(getConversation(value, s))
                }
        }
        return listConversation
    }
    private fun getConversation(snapShot: DocumentSnapshot, groupId: String): Conversation {
        val mapConversation = snapShot.data?.get(CONSTANT.KEY_CONVERSATION) as Map<*, *>
        return Conversation(mapConversation[CONSTANT.KEY_CONVERSATION_SENDER_NAME] as String,
            mapConversation[CONSTANT.KEY_CONVERSATION_CONTENT] as String,
            Inites.convertTimeStampToDate(mapConversation[CONSTANT.KEY_CONVERSATION_TIME_SEND] as Timestamp),
            groupId,
            snapShot.data?.get(CONSTANT.KEY_GROUP_NAME) as String)
    }
    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}
