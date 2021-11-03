package com.example.mymessagingapp.data

import java.util.*

data class Conversation(var senderId : String, var content : String, var timeSend : Date) : Comparable<Conversation> {
    override fun compareTo(other: Conversation): Int {
        return timeSend.compareTo(other.timeSend)
    }

}