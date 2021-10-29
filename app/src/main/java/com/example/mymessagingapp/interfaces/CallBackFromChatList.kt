package com.example.mymessagingapp.interfaces

import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import java.util.*

interface CallBackFromChatList {
    fun onGroupSelected(user : User, group : Group)
}