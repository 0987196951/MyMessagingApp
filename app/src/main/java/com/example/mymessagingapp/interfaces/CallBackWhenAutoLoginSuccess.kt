package com.example.mymessagingapp.interfaces

import com.example.mymessagingapp.data.User

interface CallBackWhenAutoLoginSuccess {
    fun onLogin(user : User)
}