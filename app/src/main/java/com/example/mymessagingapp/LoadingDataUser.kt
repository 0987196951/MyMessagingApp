package com.example.mymessagingapp

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.example.mymessagingapp.data.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoadingDataUser : Fragment() {
    private lateinit var progress : ProgressBar
    private lateinit var user : User
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
        Firebase.firestore.collection(CONSTANT.KEY_USER)
            .whereEqualTo(CONSTANT.KEY_USER_GMAIL, )
    }
    companion object {
        fun newInstance() : LoadingDataUser {
            return LoadingDataUser()
        }
    }

}