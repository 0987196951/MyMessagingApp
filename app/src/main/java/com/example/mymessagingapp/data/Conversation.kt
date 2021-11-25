package com.example.mymessagingapp.data

import java.util.*

data class Conversation(var senderName : String, var content : String, var timeSend : Date, var groupId : String, var groupName : String, var imageGroup : String) : Comparable<Conversation> {
    override fun compareTo(other: Conversation): Int {
        return timeSend.compareTo(other.timeSend)
    }

}