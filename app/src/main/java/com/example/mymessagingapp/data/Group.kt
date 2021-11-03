package com.example.mymessagingapp.data

import java.io.Serializable
import java.util.*

data class Group(val groupId : String,
                 var nameGroup : String,
                 val createdGroup: Date,
                 var member_list_id : List<String>,
                 val imageGroup : String,
                 ) : Serializable{
}