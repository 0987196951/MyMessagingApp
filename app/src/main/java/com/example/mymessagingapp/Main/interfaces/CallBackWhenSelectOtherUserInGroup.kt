package com.example.mymessagingapp.Main.interfaces

import com.example.mymessagingapp.Main.data.User

interface CallBackWhenSelectOtherUserInGroup {
    fun onUserSelect(user : User)
}