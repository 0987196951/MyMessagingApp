package com.example.mymessagingapp.data

import java.util.*

data class Conversation(var groupId : String, var lastMessage : String, var timeLastMessage : Date) : Comparable<Conversation> {
    override fun compareTo(other: Conversation): Int {
        return timeLastMessage.compareTo(other.timeLastMessage)
    }

}