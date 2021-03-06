package com.example.mymessagingapp.Main.system.data

import java.util.*

data class ChatMessage (var senderId : String, var message : String , var timeMessage : Date, var senderName : String) : Comparable<ChatMessage> {
    override fun compareTo(other: ChatMessage): Int {
        return timeMessage.compareTo(other.timeMessage)
    }

}