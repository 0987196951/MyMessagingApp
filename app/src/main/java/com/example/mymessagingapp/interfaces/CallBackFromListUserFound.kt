package com.example.mymessagingapp.interfaces

import com.example.mymessagingapp.data.User

interface CallBackFromListUserFound {
    fun onUserFound(user : User)
}