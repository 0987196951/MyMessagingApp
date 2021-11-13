package com.example.mymessagingapp.interfaces

import com.example.mymessagingapp.data.Group

interface CallBackWhenCheckGroupExist {
    fun callBackGroupExisted(groupFind : Group)
    fun callBackGroupNotExist()
}