package com.example.mymessagingapp.Main.system.interfaces

import com.example.mymessagingapp.Main.system.data.Group
import com.example.mymessagingapp.Main.system.data.User

interface CallBackAfterSelectedGroup {
    fun onGroupSelected(user : User, group : Group)
}