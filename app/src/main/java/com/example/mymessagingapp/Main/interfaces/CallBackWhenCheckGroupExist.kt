package com.example.mymessagingapp.Main.interfaces

import com.example.mymessagingapp.Main.data.Group

interface CallBackWhenCheckGroupExist {
    fun callBackGroupExisted(groupFind : Group)
    fun callBackGroupNotExist()
}