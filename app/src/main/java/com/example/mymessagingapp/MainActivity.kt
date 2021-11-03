package com.example.mymessagingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.messapp.R
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.interfaces.CallBackFromChatList
import java.util.*

class MainActivity : AppCompatActivity(), CallBackFromChatList{
    private var user : User = User("tien", "dmcsnccc@gmail.com", "asdadad", "fnionsdfoisnfoisdf")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
}