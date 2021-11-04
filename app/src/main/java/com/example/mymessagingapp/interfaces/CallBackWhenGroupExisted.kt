package com.example.mymessagingapp.interfaces

import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User

interface CallBackWhenGroupExisted {
    fun onGroupExist( group : Group)
}