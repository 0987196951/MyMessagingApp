package com.example.mymessagingapp.data

import java.util.*

data class ChatMessage (var senderId : String, var message : String , var timeMessage : Date) : Comparable<ChatMessage> {
    override fun compareTo(other: ChatMessage): Int {
        return timeMessage.compareTo(other.timeMessage)
    }

}