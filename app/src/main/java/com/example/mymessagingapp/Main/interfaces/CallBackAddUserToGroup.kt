package com.example.mymessagingapp.Main.interfaces

import com.example.mymessagingapp.Main.data.User

interface CallBackAddUserToGroup {
    fun onAddOtherUserToGroup(userAdded : User)
}