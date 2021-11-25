package com.example.mymessagingapp.interfaces

import com.example.mymessagingapp.data.User

interface CallBackWhenSelectOtherUserInGroup {
    fun onUserSelect(user : User)
}