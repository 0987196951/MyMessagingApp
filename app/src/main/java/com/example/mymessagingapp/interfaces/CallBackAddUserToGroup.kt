package com.example.mymessagingapp.interfaces

import com.example.mymessagingapp.data.User

interface CallBackAddUserToGroup {
    fun onAddOtherUserToGroup(userAdded : User)
}