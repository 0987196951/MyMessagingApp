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
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
private val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), CallBackFromListUserFound, CallBackFromMakeGroup, CallBackFromChatList, CallBackWhenSeeInfoUser{
    private lateinit var user : User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        user = User("678", "nguyen viet tien", "123456", "dmcsncc19@gmail.com", Date(), Date(), "asdawdssdfcas", false)
        val currentFragment =supportFragmentManager.findFragmentById(R.id.fragment_container)
        /*if(currentFragment == null){
            val chatListFragment = ChatListFragment.newInstance(user)
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, chatListFragment).commit()
        }*/
        if(currentFragment == null){
            val signUpFragment = SignUpFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, signUpFragment)
                .commit()
        }
    }
    override fun onUserFound(userFound: User) {
        Log.d(TAG, "call back onUserFound in Main userFound id ${userFound.userId}")
        val callback  = object :  CallBackWhenCheckGroupExist{
            override fun callBackGroupExisted(groupFind: Group) {
                Log.d(TAG, "call back group existed")
                val chatFragment = ChatFragment.newInstance(user, groupFind)
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null).commit()
            }
            override fun callBackGroupNotExist() {
                Log.d(TAG, "call back group didn't existed")
                val group = Group(UUID.randomUUID().toString(),
                    userFound.name, Date(),  false, CONSTANT.IMAGE_DEFAULT)
                val hashGroup = mapOf(
                    CONSTANT.KEY_GROUP_ID to group.groupId,
                    CONSTANT.KEY_GROUP_NAME to group.nameGroup,
                    CONSTANT.KEY_GROUP_CREATED to group.createdGroup,
                    CONSTANT.KEY_GROUP_LIST_MEMBER to listOf(user.userId, userFound.userId),
                    CONSTANT.KEY_IS_GROUP to false,
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
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, chatFragment).addToBackStack(null).commit()
            }
        }
       checkGroupIsExist(userFound, callback)
    }
    override fun onMadeGroup(group: Group) {
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
        val hashNewMessage = mapOf(
                CONSTANT.KEY_MESSAGE_SENDER_NAME to "system",
                CONSTANT.KEY_MESSAGE_CONTENT to "${user.name} is made group",
                CONSTANT.KEY_MESSAGE_TIME_SEND to Date(),
                CONSTANT.KEY_MESSAGE_SENDER_ID to CONSTANT.KEY_MESSAGE_SYSTEM_ID
        )
        val db= Firebase.firestore
        db.collection(CONSTANT.KEY_GROUP).document(group.groupId).set(hashGroup)
            db.collection(CONSTANT.KEY_GROUP).document(group.groupId).collection(CONSTANT.KEY_MESSAGE)
                .add(hashNewMessage)
        val userRef = db.collection(CONSTANT.KEY_USER)
        userRef.document(user.userId).update(CONSTANT.KEY_USER_LIST_GROUP_ID, FieldValue.arrayUnion(group.groupId))
        val chatFragment = ChatFragment.newInstance(user, group)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, chatFragment)
            .addToBackStack(null).commit()
    }
    private fun checkGroupIsExist(userFound : User, callback : CallBackWhenCheckGroupExist) {
        var groupFind : Group? = null
        Log.d(TAG, "" + user.userId + ' ' + userFound.userId)
        val db = Firebase.firestore
        db.collection(CONSTANT.KEY_GROUP)
            .whereEqualTo(CONSTANT.KEY_GROUP_IS_GROUP, false)
            .whereArrayContains(CONSTANT.KEY_GROUP_LIST_MEMBER, user.userId)
            .get()
            .addOnSuccessListener{ value ->
                if(value != null && !value.isEmpty){
                    var check = false
                    for (doc in value.documents){
                        if((doc.data?.get(CONSTANT.KEY_GROUP_LIST_MEMBER) as List<String>).contains(userFound.userId)){
                            check = true
                            groupFind = Inites.getGroup(doc as QueryDocumentSnapshot)
                            callback.callBackGroupExisted(groupFind!!)
                            break
                        }
                    }
                    if(!check){
                        callback.callBackGroupNotExist()
                    }
                }
                else {
                    Log.d(TAG, "value null")
                    callback.callBackGroupNotExist()
                }
            }

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
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, chatFragment)
                    .addToBackStack(null).commit()
            }.addOnFailureListener{ e->
                Log.d("Inites" ,"Can't get group")
            }
    }

    override fun onSeeInfoUser() {
        val infoUserFragment = MoreInfoUser.newInstance(user)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, infoUserFragment)
            .addToBackStack(null).commit()
    }
}