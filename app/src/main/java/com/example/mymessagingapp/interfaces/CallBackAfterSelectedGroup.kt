package com.example.mymessagingapp.interfaces

import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User

interface CallBackAfterSelectedGroup {
    fun onGroupSelected(user : User, group : Group)
}