package com.example.mymessagingapp.Main.data

import java.io.Serializable
import java.util.*

data class Group(
    val groupId: String,
    var nameGroup: String,
    val createdGroup: Date,
    val isGroup: Boolean,
    var imageGroup: String,
                 ) : Serializable{
}