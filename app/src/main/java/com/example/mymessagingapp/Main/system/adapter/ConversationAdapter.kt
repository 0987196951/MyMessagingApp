package com.example.mymessagingapp.Main.system.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.example.mymessagingapp.R
import com.example.mymessagingapp.Main.system.data.Conversation
import com.example.mymessagingapp.Main.system.data.User
import com.example.mymessagingapp.Main.system.interfaces.CallBackFromChatList
import com.example.mymessagingapp.Main.system.Fragment.utilities.Inites
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
private const val TAG = "ConversationAdapter"
class  ConversationAdapter(private var user: User, private var list: List<Conversation>, var context: Context, var layoutInflater: LayoutInflater) :
    RecyclerView.Adapter<ConversationAdapter.ConversationHolder>() {
    val db = Firebase.firestore
    private var mId : MutableMap<String, Conversation> = mutableMapOf()
    private var callback : CallBackFromChatList
    private var mSortedList : SortedList<Conversation> =
        SortedList(Conversation::class.java, SortedListConversationAdapter(this));
    init {
        mSortedList.addAll(list)
        callback = context as CallBackFromChatList
    }
    inner class ConversationHolder(view : View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var conversation: Conversation
        private val chatListImageGroup = itemView.findViewById(R.id.chatListImageGroup) as ImageView
        private val chatListNameGroup = itemView.findViewById(R.id.chatListNameGroup) as TextView
        private val chatListLastMessage = itemView.findViewById(R.id.chatListLastMessage) as TextView
        init {
            itemView.setOnClickListener(this)
        }
        fun bind(conversation : Conversation){
            this.conversation = conversation

            if(conversation.content.length < 20 ){
                chatListLastMessage.text = "${conversation.senderName} : ${conversation.content}"
            }
            else {
                chatListLastMessage.text = "${conversation.senderName} : ${conversation.content.substring(0, 20)}..."
            }
            if(conversation.senderName.isEmpty()) chatListLastMessage.visibility = View.GONE
            chatListNameGroup.text = conversation.groupName
            chatListImageGroup.setImageBitmap(Inites.getImage(conversation.imageGroup))
        }
        override fun onClick(view : View?) {
            (context as CallBackFromChatList).onGroupSelected(conversation.groupId)
        }
    }
    fun addConversation(con : Conversation){
        var con1 = mId.get(con.groupId)
        if(con1 == null){
            mSortedList.add(con)
        }
        else {
            mSortedList.updateItemAt(mSortedList.indexOf(con1), con)
        }
        mId.put(con.groupId, con)
    }
    fun removeConversation(con : Conversation){
        var con1 = mId.get(con.groupId)
        if(con1 == null){
            mSortedList.add(con)
        }
        else {
            mSortedList.removeItemAt(mSortedList.indexOf(con1))
        }
        mId.put(con.groupId, con)
    }
    fun removeConversationAt(index : Int){
        mSortedList.removeItemAt(index)
    }
    fun getSortedList() : SortedList<Conversation> {
        return mSortedList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationHolder {
        val view =layoutInflater.inflate(R.layout.chat_list_conversation, parent, false)
        return ConversationHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationHolder, position: Int) {
        holder.bind(mSortedList[position])
    }

    override fun getItemCount(): Int {
        return mSortedList.size()
    }
}

