package com.example.mymessagingapp.Main.interfaces

import com.example.mymessagingapp.Main.data.User

interface CallBackWhenSignIn {
    fun onSignInSuccess(user : User)
    fun onSignInNotSuccess()
}