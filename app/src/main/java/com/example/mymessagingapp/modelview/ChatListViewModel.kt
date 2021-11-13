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
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
private val TAG = "ChatListViewModel"
class ChatListViewModelFactory(
    val user: User,
    private val context: Context?,
    private val layoutInflater: LayoutInflater  ) : ViewModelProvider.Factory {
    private val db = Firebase.firestore
    val conversationAdapter : MutableLiveData<ConversationAdapter>
        = MutableLiveData(ConversationAdapter(user,getConversationAndAddSnapShot(), context!!, layoutInflater))
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(User::class.java, Context::class.java, LayoutInflater::class.java)
            .newInstance(user, context, layoutInflater)
    }
    private fun getConversationAndAddSnapShot() :MutableList<Conversation>{
        var listGroupId : MutableList<String> = mutableListOf()
        db.collection(CONSTANT.KEY_USER).document(user.userId).get().addOnSuccessListener { value ->
            if(value != null){
                listGroupId.addAll(value.data?.get(CONSTANT.KEY_USER_LIST_GROUP_ID) as MutableList<String>)
            }
        }.addOnFailureListener {
            Log.d(TAG, "You can't init list groupId")
        }.continueWith {
            db.collection(CONSTANT.KEY_GROUP).whereArrayContains(CONSTANT.KEY_GROUP_LIST_MEMBER, user.userId)
                .addSnapshotListener EventListener@{ snapShot, e ->
                    if(e != null){
                        Log.d(TAG, "error when listen")
                    }
                    if(snapShot != null ){
                        for(doc in snapShot.documentChanges){
                            if( doc.type == DocumentChange.Type.MODIFIED || doc.type == DocumentChange.Type.ADDED){
                                var mapConversation = doc.document[CONSTANT.KEY_CONVERSATION] as Map<* , *>
                                var groupId = doc.document[CONSTANT.KEY_GROUP_ID] as String
                                conversationAdapter.value?.addConversation(Conversation(mapConversation[CONSTANT.KEY_CONVERSATION_SENDER_NAME] as String,
                                    mapConversation[CONSTANT.KEY_CONVERSATION_CONTENT] as String,
                                    Inites.convertTimeStampToDate(mapConversation[CONSTANT.KEY_CONVERSATION_TIME_SEND] as Timestamp),
                                    groupId,
                                    doc.document.get(CONSTANT.KEY_GROUP_NAME) as String))
                            }
                            else if(doc.type == DocumentChange.Type.REMOVED){
                                var mapConversation = doc.document[CONSTANT.KEY_CONVERSATION] as Map<* , *>
                                var groupId = doc.document[CONSTANT.KEY_GROUP_ID] as String
                                conversationAdapter.value?.removeConversation(Conversation(mapConversation[CONSTANT.KEY_CONVERSATION_SENDER_NAME] as String,
                                    mapConversation[CONSTANT.KEY_CONVERSATION_CONTENT] as String,
                                    Inites.convertTimeStampToDate(mapConversation[CONSTANT.KEY_CONVERSATION_TIME_SEND] as Timestamp),
                                    groupId,
                                    doc.document.get(CONSTANT.KEY_GROUP_NAME) as String))
                            }
                        }
                        conversationAdapter.notifyObserver()
                    }
                }
        }

        return mutableListOf()
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
