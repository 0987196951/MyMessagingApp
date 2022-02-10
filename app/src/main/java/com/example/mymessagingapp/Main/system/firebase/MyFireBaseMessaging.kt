package com.example.mymessagingapp.Main.system.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class MyFireBaseMessaging : FirebaseMessagingService() {
    override fun onNewToken(token : String) {
        Log.d("TAG", "new token  is $token")
    }
    /*fun Send(notification : String) : Task
    {
        var fcmKey = "Legacy server key"
        var http = HttpClient();
        http.DefaultRequestHeaders.TryAddWithoutValidation("Authorization", "key=" + fcmKey);
        http.DefaultRequestHeaders.TryAddWithoutValidation("content-length", notification.Length.ToString());
        var content = new StringContent(notification, System.Text.Encoding.UTF8, "application/json");
        var response = await http.PostAsync("https://fcm.googleapis.com/fcm/send", content);
    }*/
}