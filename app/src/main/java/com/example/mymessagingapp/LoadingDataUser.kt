package com.example.mymessagingapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.interfaces.CallBackWhenLoginSuccess
import com.example.mymessagingapp.interfaces.CallBackWhenLoginAutoNotSuccess
import com.example.mymessagingapp.utilities.Inites
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.FileInputStream

private const val TAG = "LoadingDataUser"
class LoadingDataUser : Fragment() {
    private lateinit var progress : ProgressBar
    private lateinit var user : User
    private val fileName = "myaccount.txt"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.loading_data_user, container, false)
        progress = view.findViewById(R.id.progressBar2) as ProgressBar
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progress.visibility = View.VISIBLE
        lateinit var gmail : String
        lateinit var password : String
        var fileInput : FileInputStream? = null
        try {
            fileInput = context?.openFileInput(fileName)
        }catch (e : Exception){
            (requireContext() as CallBackWhenLoginAutoNotSuccess).onSignIn()
            return
        }
        //Log.d(TAG, "file name $fileName" + fileInput?.available())
        if(fileInput?.available()!! > 0){
            context?.openFileInput(fileName)?.bufferedReader()?.useLines {  lines ->
                var it = lines.iterator()
                gmail = it.next()
                Log.d(TAG, "" + gmail)
                password = it.next()
            }
            //Log.d(TAG, "gmail + $gmail\npassword + $password")
            Firebase.firestore.collection(CONSTANT.KEY_USER)
                .whereEqualTo(CONSTANT.KEY_USER_GMAIL, gmail.trim())
                .whereEqualTo(CONSTANT.KEY_USER_PASSWORD, password.trim())
                .get().addOnSuccessListener { value ->
                    if(value != null && !value.isEmpty){
                        for (doc in value.documents){
                            user = Inites.getUser(doc)
                        }
                        (requireContext() as CallBackWhenLoginSuccess).onLoginSuccess(user, 2)
                        progress.visibility = View.GONE
                    }
                    else {
                        (requireContext() as CallBackWhenLoginAutoNotSuccess).onSignIn()
                    }
                }
        }
        else {
                //Log.d(TAG, "Can\'t open file")
            (requireContext() as CallBackWhenLoginAutoNotSuccess).onSignIn()
        }
    }
    companion object {
        fun newInstance() : LoadingDataUser {
            return LoadingDataUser()
        }
    }

}