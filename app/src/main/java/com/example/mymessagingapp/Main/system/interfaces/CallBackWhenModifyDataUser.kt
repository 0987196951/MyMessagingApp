package com.example.mymessagingapp.Main.system.interfaces

import com.example.mymessagingapp.Main.system.data.User

interface CallBackWhenModifyDataUser {
    fun onModify(user : User)
}