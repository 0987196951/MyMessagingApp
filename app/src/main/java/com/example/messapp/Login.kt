package com.example.messapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.login.*


class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        btlogin1.setOnClickListener {
            val username_login = username.text.toString()
            val password_login = password.text.toString()
            Log.d("username",username_login)
            Log.d("password",password_login)

        }
        btsignin1.setOnClickListener {
            val intent = Intent(this,Signup::class.java)
            startActivity(intent)
        }
    }

}