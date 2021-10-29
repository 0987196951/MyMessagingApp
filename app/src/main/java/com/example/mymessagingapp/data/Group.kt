package com.example.mymessagingapp.data

import java.io.Serializable
import java.util.*

data class Group(val groupId : UUID, val nameGroup : String, val imageGroup : String, val dateInit : Date , val listMember : List<String>) : Serializable{
}