package com.example.mymessagingapp.Main.interfaces

import com.example.mymessagingapp.Main.data.Group
import com.example.mymessagingapp.Main.data.User

interface CallBackAfterSelectedGroup {
    fun onGroupSelected(user : User, group : Group)
}