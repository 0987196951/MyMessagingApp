package com.example.mymessagingapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.interfaces.*
import com.example.mymessagingapp.utilities.Inites
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
private val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), CallBackFromListUserFound, CallBackFromMakeGroup, CallBackFromChatList,
    CallBackWhenSeeInfoUser, CallBackWhenLoginSuccess, CallBackWhenLoginAutoNotSuccess, CallBackWhenModifyDataUser,
    CallBackWhenOutGroup, CallBackWhenSeeMoreInfoGroup,CallBackWhenLogOut, CallBackWhenSignUp {
    private lateinit var user : User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val currentFragment =supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(currentFragment == null){
            val loadingDataUser = LoadingDataUser.newInstance()
            val signInFragment = SignInFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, signInFragment)
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, loadingDataUser)
                .addToBackStack(null)
                .commit()
        }
    }
    override fun onLoginSuccess(user : User, pos : Int) {
        this.user = user
        /*FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result//ecDf8P2BQousElA_bSocSG:APA91bE4htWRI5CHW0c9j5lBEOSrtCfnK46lb3nThPIfKq3vlCXJtuROEb7Emjw_v2S2HhFhEKie9qpFXpwiNS6W8sh4ZQtlxopbsxv-N9rTBBORruF3Bl2GfuPLuaaX56Hrw827ozxD
            Log.d(TAG, "" + token)
        })*/
        val chatListFragment = ChatListFragment.newInstance(user)
        if(pos == 1 ) supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, chatListFragment).addToBackStack(null).commit()
        else supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, chatListFragment).commit()
    }
    override fun onSignIn() {
        val signInFragment = SignInFragment.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, signInFragment)
                 .commit()
    }
    override fun onSignUp() {
        val signUpFragment = SignUpFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, signUpFragment).addToBackStack(null).commit()
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
                    userFound.name, Date(),  false,
                    userFound.image)
                val groupMapping = Group(UUID.randomUUID().toString(), user.name,Date(), false, user.image)
                makeAChat1_1(user, group, userFound.userId, groupMapping.groupId)
                makeAChat1_1(userFound, groupMapping, user.userId, group.groupId)
                val chatFragment = ChatFragment.newInstance(user, group)
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, chatFragment)
                    .addToBackStack(null).commit()
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
            .whereEqualTo(CONSTANT.KEY_GROUP_RECEIVER, userFound.userId)
            .whereArrayContains(CONSTANT.KEY_GROUP_LIST_MEMBER, user.userId)
            .get()
            .addOnSuccessListener{ value ->
                if(value != null && !value.isEmpty){
                    for (doc in value.documents){
                        groupFind = Inites.getGroup(doc as QueryDocumentSnapshot)
                        callback.callBackGroupExisted(groupFind!!)
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
                    value.data?.get(CONSTANT.KEY_GROUP_IS_GROUP) as Boolean,
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
        Log.d(TAG, "on See Info User")
        val infoUserFragment = MoreInfoUser.newInstance(user)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, infoUserFragment)
            .addToBackStack(null).commit()
    }

    override fun onModify(user: User) {
        val modifyDataUser = ModifyInfoUserFragment.newInstance(user)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, modifyDataUser)
            .addToBackStack(null).commit()
    }

    override fun outGroup(group : Group) {
        Log.d(TAG, "out group")
        val db = Firebase.firestore
        db.collection(CONSTANT.KEY_USER).document(user.userId)
            .update(CONSTANT.KEY_USER_LIST_GROUP_ID, FieldValue.arrayRemove(group.groupId))
        db.collection(CONSTANT.KEY_GROUP).document(group.groupId)
            .update(CONSTANT.KEY_GROUP_LIST_MEMBER, FieldValue.arrayRemove(user.userId))
            supportFragmentManager.popBackStack()
            supportFragmentManager.popBackStack()
    }

    override fun seeForInfoGroup(group : Group) {
        val fragment = MoreInfoGroup.newInstance(group, user)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .addToBackStack(null).commit()
    }

    override fun onLogout() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val signInFragment = SignInFragment.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, signInFragment)
            .commit()
        val fileName = "myaccount.txt"
        this.openFileOutput(fileName, Context.MODE_PRIVATE).use{
            it?.write("".toByteArray())
        }
    }
    fun makeAChat1_1(user : User, group:Group,receiver : String, groupMapping : String){
        val hashGroup = mapOf(
            CONSTANT.KEY_GROUP_ID to group.groupId,
            CONSTANT.KEY_GROUP_NAME to group.nameGroup,
            CONSTANT.KEY_GROUP_CREATED to group.createdGroup,
            CONSTANT.KEY_GROUP_LIST_MEMBER to listOf(user.userId),
            CONSTANT.KEY_IS_GROUP to false,
            CONSTANT.KEY_IMAGE_GROUP to group.imageGroup,
            CONSTANT.KEY_GROUP_RECEIVER to receiver,
            CONSTANT.KEY_GROUP_MAPPING to groupMapping,
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
    }
}