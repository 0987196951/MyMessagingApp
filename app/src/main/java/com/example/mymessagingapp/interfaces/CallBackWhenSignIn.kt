package com.example.mymessagingapp.interfaces

import com.example.mymessagingapp.data.User

interface CallBackWhenSignIn {
    fun onSignInSuccess(user : User)
    fun onSignInNotSuccess()
}