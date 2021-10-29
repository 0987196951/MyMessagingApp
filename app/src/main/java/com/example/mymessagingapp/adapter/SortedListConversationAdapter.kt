package com.example.mymessagingapp.adapter

import androidx.recyclerview.widget.SortedListAdapterCallback
import com.example.mymessagingapp.data.Conversation

class SortedListConversationAdapter(adapter: ConversationAdapter) : SortedListAdapterCallback<Conversation>( adapter) {
    override fun compare(con1 : Conversation?, con2: Conversation?): Int {
            return con1!!.timeLastMessage.compareTo(con2!!.timeLastMessage)
    }

    override fun areContentsTheSame(oldItem: Conversation?, newItem: Conversation?): Boolean {
        return oldItem!!.groupId.equals(newItem!!.groupId)
    }

    override fun areItemsTheSame(item1: Conversation?, item2: Conversation?): Boolean {
        return item1!!.groupId.equals(item2!!.groupId)
    }
}