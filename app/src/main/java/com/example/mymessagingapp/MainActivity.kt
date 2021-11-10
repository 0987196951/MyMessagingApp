package com.example.mymessagingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.interfaces.*
import com.example.mymessagingapp.utilities.Inites
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
private val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), CallBackFromListUserFound, CallBackWhenGroupExisted, CallBackFromMakeGroup, CallBackAfterSelectedGroup, CallBackFromChatList{
    private lateinit var user : User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        user = User("234", "nguyen", "123456", "dmcsncc19@gmail.com", Date(), Date(), "asdawdssdfcas", false)
        val currentFragment =supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(currentFragment == null){
            val chatListFragment = ChatListFragment.newInstance(user)
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, chatListFragment).commit()
        }
    }
    fun onUserLogin(user : User){

    }
    override fun onGroupSelected(user : User, group : Group){
        val chatFragment = ChatFragment.newInstance(user, group)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, chatFragment).commit()
    }

    override fun onUserFound(userFound: User) {
        val group = Group(UUID.randomUUID().toString(), userFound.name, Date(),  false, CONSTANT.IMAGE_DEFAULT)
        val hashGroup = mapOf(
            CONSTANT.KEY_GROUP_ID to group.groupId,
            CONSTANT.KEY_GROUP_NAME to group.nameGroup,
            CONSTANT.KEY_GROUP_CREATED to group.createdGroup,
            CONSTANT.KEY_GROUP_LIST_MEMBER to listOf(user.userId, userFound.userId),
            CONSTANT.KEY_IS_GROUP to group.isGroup,
            CONSTANT.KEY_IMAGE_GROUP to group.imageGroup,
            CONSTANT.KEY_CONVERSATION to mapOf(
                CONSTANT.KEY_CONVERSATION_SENDER_NAME to "",
                CONSTANT.KEY_CONVERSATION_CONTENT to "",
                CONSTANT.KEY_CONVERSATION_TIME_SEND to Date()
            )
        )
        val db= Firebase.firestore
        db.collection(CONSTANT.KEY_GROUP).document(group.groupId).set(hashGroup)
        db.collection(CONSTANT.KEY_GROUP).document(group.groupId).collection(CONSTANT.KEY_MESSAGE)
        val userRef = db.collection(CONSTANT.KEY_USER)
        userRef.document(user.userId).update(CONSTANT.KEY_USER_LIST_GROUP_ID, FieldValue.arrayUnion(group.groupId))
        userRef.document(userFound.userId).update(CONSTANT.KEY_USER_LIST_GROUP_ID, FieldValue.arrayUnion(group.groupId))
        val chatFragment = ChatFragment.newInstance(user, group)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, chatFragment).commit()
    }

    override fun onGroupExist( group: Group) {
        val chatFragment = ChatFragment.newInstance(user, group)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, chatFragment).commit()
    }

    override fun onMadeGroup(groupMade: Group) {
        val group = Group(UUID.randomUUID().toString(), groupMade.nameGroup, Date(),  false, CONSTANT.IMAGE_DEFAULT)
        val hashGroup = mapOf(
            CONSTANT.KEY_GROUP_ID to group.groupId,
            CONSTANT.KEY_GROUP_NAME to group.nameGroup,
            CONSTANT.KEY_GROUP_CREATED to group.createdGroup,
            CONSTANT.KEY_GROUP_LIST_MEMBER to listOf(user.userId),
            CONSTANT.KEY_IS_GROUP to group.isGroup,
            CONSTANT.KEY_IMAGE_GROUP to group.imageGroup,
            CONSTANT.KEY_CONVERSATION to mapOf(
                CONSTANT.KEY_CONVERSATION_SENDER_NAME to "",
                CONSTANT.KEY_CONVERSATION_CONTENT to "",
                CONSTANT.KEY_CONVERSATION_TIME_SEND to Date()
            )
        )
        val db= Firebase.firestore
        db.collection(CONSTANT.KEY_GROUP).document(group.groupId).set(hashGroup)
        db.collection(CONSTANT.KEY_GROUP).document(group.groupId).collection(CONSTANT.KEY_MESSAGE)
        val userRef = db.collection(CONSTANT.KEY_USER)
        userRef.document(user.userId).update(CONSTANT.KEY_USER_LIST_GROUP_ID, FieldValue.arrayUnion(group.groupId))

        //serRef.document(userFound.userId).update(CONSTANT.KEY_USER_LIST_GROUP_ID, FieldValue.arrayUnion(group.groupId))
        val chatFragment = ChatFragment.newInstance(user, groupMade)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, chatFragment)
            .addToBackStack(null).commit()
    }

    override fun onGroupSelected(groupId: String) {
        lateinit var group : Group
        Firebase.firestore.collection(CONSTANT.KEY_GROUP).document(groupId).get()
            .addOnSuccessListener { value ->
                group = Group(groupId, value.data?.get(CONSTANT.KEY_GROUP_NAME) as String,
                    Inites.convertTimeStampToDate(value.data?.get(CONSTANT.KEY_GROUP_CREATED) as Timestamp),
                    true,
                    value.data?.get(CONSTANT.KEY_GROUP_IMAGE) as String
                )
                val chatFragment = ChatFragment.newInstance(user, group)
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, chatFragment).addToBackStack(null).commit()
            }.addOnFailureListener{ e->
                Log.d("Inites" ,"Can't get group")
            }
    }
}