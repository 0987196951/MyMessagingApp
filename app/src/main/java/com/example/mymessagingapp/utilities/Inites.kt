package com.example.mymessagingapp.utilities

import android.util.Log
import com.example.mymessagingapp.CONSTANT
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
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
        fun getUser(doc : DocumentSnapshot) : User{
            return User(doc.data?.get(CONSTANT.KEY_USER_ID) as  String,
                doc.data!![CONSTANT.KEY_USER_NAME] as  String,
                doc.data!![CONSTANT.KEY_USER_PASSWORD] as String,
                doc.data!![CONSTANT.KEY_USER_GMAIL] as String,
                convertTimeStampToDate(doc.data!![CONSTANT.KEY_USER_DATE_OF_BIRTH] as Timestamp),
                convertTimeStampToDate(doc.data!![CONSTANT.KEY_USER_CREATE_ACCOUNT] as Timestamp),
                doc.data!![CONSTANT.KEY_USER_IMAGE] as String,
                true
            )
        }
        fun getGroup(doc : QueryDocumentSnapshot) : Group{
            return Group(doc.data[CONSTANT.KEY_GROUP_ID] as String,
                doc.data[CONSTANT.KEY_GROUP_NAME] as String,
                convertTimeStampToDate(doc.data[CONSTANT.KEY_GROUP_CREATED] as Timestamp),
                doc.data[CONSTANT.KEY_GROUP_IS_GROUP] as Boolean,
                doc.data[CONSTANT.KEY_GROUP_IMAGE] as String
            )
        }
        fun parseDateFromString(s : String) : Date?{
            val formatter = SimpleDateFormat("dd/mm/yyyy")
            return formatter.parse(s)
        }
    }
}