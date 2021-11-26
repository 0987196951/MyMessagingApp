package com.example.mymessagingapp.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class MyFireBaseMessaging : FirebaseMessagingService() {
    override fun onNewToken(token : String) {
        Log.d("TAG", "new token  is $token")

    }
}