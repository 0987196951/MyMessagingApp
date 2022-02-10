package com.example.mymessagingapp.Main.interfaces

import com.example.mymessagingapp.Main.data.User

interface CallBackWhenLoginSuccess {
    fun onLoginSuccess(user : User, pos : Int)
}