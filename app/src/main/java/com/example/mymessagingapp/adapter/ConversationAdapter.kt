package com.example.mymessagingapp.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.example.mymessagingapp.ChatListFragment
import com.example.mymessagingapp.R
import com.example.mymessagingapp.data.Conversation
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.interfaces.CallBackFromChatList
import com.example.mymessagingapp.utilities.Inites
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
private const val TAG = "ConversationAdapter"
class  ConversationAdapter(private var user: User, private var list: List<Conversation>, var context: Context, var layoutInflater: LayoutInflater) :
    RecyclerView.Adapter<ConversationAdapter.ConversationHolder>() {
    val db = Firebase.firestore
    private var mId : HashMap<String, Conversation> = hashMapOf()
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
            chatListLastMessage.text = conversation.senderName + " : " + conversation.content
            if(conversation.senderName.isEmpty()) chatListLastMessage.visibility = View.GONE
            chatListNameGroup.text = conversation.groupName
            chatListImageGroup.setImageBitmap(Inites.getImage(conversation.imageGroup))
            Log.d(TAG, "conversation id : " + conversation.groupId)
        }
        override fun onClick(view : View?) {
            (context as CallBackFromChatList).onGroupSelected(conversation.groupId)
        }
    }
    fun addConversation(con : Conversation){
        var con1 = mId.get(con.groupId)
        if(con1 == null){
            mSortedList.add(con)
            mId.put(con.groupId, con)
        }
        else {
            mSortedList.updateItemAt(mSortedList.indexOf(con1), con)
        }
    }
    fun removeConversation(con : Conversation){
        var con1 = mId.get(con.groupId)
        if(con1 == null){
            mSortedList.add(con)
            mId.put(con.groupId, con)
        }
        else {
            mSortedList.removeItemAt(mSortedList.indexOf(con1))
        }
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

