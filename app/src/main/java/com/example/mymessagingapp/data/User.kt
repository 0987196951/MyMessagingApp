package com.example.mymessagingapp.data

import java.io.Serializable
import java.util.*

data class User(val userId : String ,val name : String, val gmail : String, val image : String, val fcm_token : String) : Serializable{

}