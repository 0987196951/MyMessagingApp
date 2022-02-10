package com.example.mymessagingapp.Main.interfaces

import com.example.mymessagingapp.Main.data.User

interface CallBackFromListUserFound {
    fun onUserFound(userFound : User)
}