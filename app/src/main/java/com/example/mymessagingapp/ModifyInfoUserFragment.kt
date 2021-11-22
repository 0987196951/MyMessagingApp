package com.example.mymessagingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.interfaces.CallBackWhenCheckInvalidSignUpOrModifyInfo
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ModifyInfoUserFragment : Fragment() {
    private lateinit var user : User
    private var encodedImage: String? = null
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var imageProfile : ImageView
    private lateinit var inputName : EditText
    private lateinit var inputDate : EditText
    private lateinit var inputEmail : EditText
    private lateinit var inputPassword : EditText
    private lateinit var inputConfirmPassword : EditText
    private lateinit var acceptModifyButton : Button
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = arguments?.getSerializable(CONSTANT.KEY_USER) as User
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modify_info_user_layout, container, false)
        imageProfile = view.findViewById(R.id.imageProfile) as ImageView
        inputName = view.findViewById(R.id.inputName) as EditText
        inputDate = view.findViewById(R.id.inputDate) as EditText
        inputEmail = view.findViewById(R.id.inputEmail) as EditText
        inputPassword = view.findViewById(R.id.inputPassword) as EditText
        inputConfirmPassword = view.findViewById(R.id.inputConfirmPassword) as EditText
        progressBar = view.findViewById(R.id.progressBar) as ProgressBar
        acceptModifyButton = view.findViewById(R.id.accept_modify) as Button
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageProfile.setImageBitmap(getImage(user.image))
        inputName.setText(user.name, TextView.BufferType.EDITABLE)
        inputDate.setText(user.dateOfBirth.toString(), TextView.BufferType.EDITABLE)
        inputEmail.setText( user.gmail, TextView.BufferType.EDITABLE)
        inputPassword.setText(user.password)
        acceptModifyButton.setOnClickListener {
        }
    }
    fun checkInvalid(callback : CallBackWhenCheckInvalidSignUpOrModifyInfo) {
        Firebase.firestore.collection(CONSTANT.KEY_USER_GMAIL)
            .whereEqualTo(CONSTANT.KEY_USER_GMAIL, inputEmail.text.toString())
            .get()
            .addOnSuccessListener { value ->
                if(value != null && !value.isEmpty){
                    callback.onValid()
                }
                else {
                    callback.onInvalid()
                }
            }
    }
    companion object {
        fun newInstance(user : User) : ModifyInfoUserFragment{
            val args = Bundle().apply {
                putSerializable(CONSTANT.KEY_USER, user)
            }
            return ModifyInfoUserFragment().apply {
                arguments = args
            }
        }
    }
    private fun getImage(encodeImage : String) : Bitmap {
        val bytes = Base64.decode(encodeImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}