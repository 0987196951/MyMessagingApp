package com.example.mymessagingapp.Main.system.interfaces

import com.example.mymessagingapp.Main.system.data.Group

interface CallBackWhenCheckGroupExist {
    fun callBackGroupExisted(groupFind : Group)
    fun callBackGroupNotExist()
}