package com.example.mymessagingapp.Main.system.interfaces

import com.example.mymessagingapp.Main.system.data.User

interface CallBackWhenLoginSuccess {
    fun onLoginSuccess(user : User, pos : Int)
}