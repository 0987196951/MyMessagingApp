package com.example.mymessagingapp.Main.system.interfaces

import com.example.mymessagingapp.Main.system.data.User

interface CallBackWhenSignIn {
    fun onSignInSuccess(user : User)
    fun onSignInNotSuccess()
}