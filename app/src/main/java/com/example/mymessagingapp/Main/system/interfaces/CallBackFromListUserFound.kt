package com.example.mymessagingapp.Main.system.interfaces

import com.example.mymessagingapp.Main.system.data.User

interface CallBackFromListUserFound {
    fun onUserFound(userFound : User)
}