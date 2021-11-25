package com.example.mymessagingapp.interfaces

import com.example.mymessagingapp.data.User

interface CallBackWhenLoginSuccess {
    fun onLoginSuccess(user : User, pos : Int)
}