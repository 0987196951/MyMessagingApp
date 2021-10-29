package com.example.mymessagingapp.modelview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
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
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*

class ChatListViewModelFactory(val user : User, private val context: Context, private val layoutInflater: LayoutInflater  ) : ViewModelProvider.Factory {
    private val db = Firebase.firestore
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(User::class.java, Context::class.java, LayoutInflater::class.java)
            .newInstance(user, context, layoutInflater)
    }
    val conversationAdapter : ConversationAdapter = ConversationAdapter(user,makeConversation(), context, layoutInflater)
    private fun makeConversation() : MutableList<Conversation>{
        var listConversation : MutableList<Conversation> = mutableListOf<Conversation>()
        var listGroupContainUser : List<String> = emptyList()
        db.collection(CONSTANT.KEY_GROUP).whereArrayContains("listMember", user.name)
            .get()
            .addOnSuccessListener { values ->
                if(values != null){
                    for(doc in values){
                        listGroupContainUser.plus(doc.id)
                    }
                }
            }.addOnFailureListener{
                Log.d("TAG", "you can init list Group Container User")
                return@addOnFailureListener
            }
        db.collection(CONSTANT.KEY_CONVERSATION).whereIn("groupId", listGroupContainUser)
            .get().addOnSuccessListener { value ->
                for(doc in value){
                    val con = doc.toObject<Conversation>()
                    listConversation.add(con)
                }
            }.addOnFailureListener{ e->
                Log.d("ChatListViewModel", "" + e.printStackTrace())
            }
        db.collection(CONSTANT.KEY_CONVERSATION)
            .whereIn("groupId", listGroupContainUser)
            .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if(e != null) {
                    return@EventListener
                }
                if(value != null) {
                    for(documentChange in value.documentChanges){
                        if(documentChange.type == DocumentChange.Type.ADDED || documentChange.type == DocumentChange.Type.MODIFIED){
                            val groupID : String = documentChange.document.getString("groupId")!!
                            val lastMes : String = documentChange.document.getString("lastMessage")!!
                            val timeLastMes : Date = documentChange.document.getDate("timeLastMessage")!!
                            conversationAdapter.addConversation(Conversation(groupID, lastMes, timeLastMes))
                        }
                        else if(documentChange.type == DocumentChange.Type.REMOVED){
                            conversationAdapter.removeConversation(documentChange.document.toObject<Conversation>())
                        }
                    }
                }
            })
        return listConversation
    }
}