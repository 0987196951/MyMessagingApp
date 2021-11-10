package com.example.mymessagingapp.utilities

import android.util.Log
import com.example.mymessagingapp.CONSTANT
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class Inites {
    companion object {
        fun initForUser(map : Map<String, Any>) : User {
            return User(
                map[CONSTANT.KEY_USER_ID] as String,
                map[CONSTANT.KEY_USER_NAME] as String,
                map[CONSTANT.KEY_USER_PASSWORD] as String,
                map[CONSTANT.KEY_USER_GMAIL] as String,
                convertTimeStampToDate(map[CONSTANT.KEY_USER_DATE_OF_BIRTH] as Timestamp),
                convertTimeStampToDate(map[CONSTANT.KEY_USER_CREATE_ACCOUNT] as Timestamp),
                map[CONSTANT.KEY_USER_IMAGE] as String,
                map[CONSTANT.KEY_USER_IS_ACTIVE] as Boolean
            )
        }
        fun convertTimeStampToDate(time : Timestamp) : Date{
            val millisecond = time.seconds * 1000 + time.nanoseconds / 1000000
            return Date(millisecond)
        }
        fun getGroup(groupId : String) : Group?{
            var group : Group? = null
            Firebase.firestore.collection(CONSTANT.KEY_GROUP).document(groupId).get()
                .addOnSuccessListener { value ->
                     group = Group(groupId, value.data?.get(CONSTANT.KEY_GROUP_NAME) as String,
                        convertTimeStampToDate(value.data?.get(CONSTANT.KEY_GROUP_CREATED) as Timestamp),
                         true,
                         value.data?.get(CONSTANT.KEY_GROUP_IMAGE) as String
                    )
                }.addOnFailureListener{ e->
                    Log.d("Inites" ,"Can't get group")
                }
            return group
        }
    }
}