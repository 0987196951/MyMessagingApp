package com.example.mymessagingapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.mymessagingapp.interfaces.CallBackWhenCheckInvalidSignUpOrModifyInfo
import com.example.mymessagingapp.interfaces.CallBackWhenLoginAutoNotSuccess
import com.example.mymessagingapp.utilities.Inites
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.HashMap

private val RESULT_OK = 1
private val TAG = "SignUpFragment"
class SignUpFragment : Fragment() {
    private var encodedImage: String? = null
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var imageProfile : ImageView
    private lateinit var textAddImage : TextView
    private lateinit var inputName : EditText
    private lateinit var inputDate : EditText
    private lateinit var inputEmail : EditText
    private lateinit var inputPassword : EditText
    private lateinit var inputConfirmPassword : EditText
    private lateinit var buttonSignUp : Button
    private lateinit var progressBar : ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_sign_up, container, false)
        imageProfile = view.findViewById(R.id.imageProfile) as ImageView
        textAddImage = view.findViewById(R.id.textAddImage) as TextView
        inputName = view.findViewById(R.id.inputName) as EditText
        inputDate = view.findViewById(R.id.inputDate) as EditText
        inputEmail = view.findViewById(R.id.inputEmail) as EditText
        inputPassword = view.findViewById(R.id.inputPassword) as EditText
        inputConfirmPassword = view.findViewById(R.id.inputConfirmPassword) as EditText
        buttonSignUp = view.findViewById(R.id.buttonSignUp) as Button
        progressBar = view.findViewById(R.id.progressBar) as ProgressBar
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListeners()
    }
    private fun setListeners() {
        buttonSignUp.setOnClickListener { v ->
            loading(true)
            val callBackInvalid = object: CallBackWhenCheckInvalidSignUpOrModifyInfo{
                override fun onValid() {
                    Toast.makeText(context, "make account success", Toast.LENGTH_SHORT).show()
                    signUp()
                }

                override fun onInvalid() {
                    Toast.makeText(context, inputEmail.text.toString() + "is already existed", Toast.LENGTH_SHORT).show()
                }
            }
            isValidSignUpDetails(callBackInvalid)
        }
        imageProfile.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, RESULT_OK)
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        loading(true)
        val database = Firebase.firestore
        val user = HashMap<String, Any?>()
        val userId = UUID.randomUUID().toString()
        user[CONSTANT.KEY_USER_ID] = userId
        user[CONSTANT.KEY_USER_NAME] = inputName.text.toString()
        user[CONSTANT.KEY_USER_DATE_OF_BIRTH] = Inites.parseDateFromString(inputDate.text.toString())
        user[CONSTANT.KEY_USER_GMAIL] = inputEmail.text.toString()
        user[CONSTANT.KEY_USER_PASSWORD] = inputPassword.text.toString()
        user[CONSTANT.KEY_USER_IMAGE] = encodedImage
        user[CONSTANT.KEY_USER_LIST_GROUP_ID] = emptyList<String>()
        user[CONSTANT.KEY_USER_CREATE_ACCOUNT] = Date()
        user[CONSTANT.KEY_USER_IS_ACTIVE] = false
        user[CONSTANT.KEY_USER_FCM_TOKEN] = ""
        database.collection(CONSTANT.KEY_USER)
            .document(userId).set(user)
            .addOnSuccessListener { value ->
                loading(false)
                (requireContext() as CallBackWhenLoginAutoNotSuccess).onSignIn()
            }
            .addOnFailureListener { exception: Exception ->
                loading(false)
                showToast(exception.message)
            }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK)
            return
        Log.d(TAG, "alo alo")
        if (requestCode == RESULT_OK && data != null) {
            val imageUri = data.data
            try {
                val inputStream = requireActivity().contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageProfile.setImageBitmap(bitmap)
                textAddImage.visibility = View.GONE
                encodedImage = encodeImage(bitmap)
                Log.d(TAG, "encode Image is : " + encodedImage)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun isValidSignUpDetails(callback : CallBackWhenCheckInvalidSignUpOrModifyInfo){
        if (encodedImage == null) {
            loading(false)
            showToast("Select profile image")
            return
        } else if (inputName.text.toString().trim().isEmpty()) {
            loading(false)
            showToast("Enter Name")
            return
        }
        else if (inputDate.text.toString().trim().isEmpty()) {
            loading(false)
            showToast("Enter Date of Birth")
        }
        else if (inputEmail.text.toString().trim().isEmpty()) {
            loading(false)
            showToast("Enter Email")
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.text.toString()).matches()) {
            loading(false)
            showToast("Enter valid Email")
            return
        } else if (inputPassword.text.toString().trim().isEmpty()) {
            loading(false)
            showToast("Enter Password")
            return
        } else if (inputConfirmPassword.text.toString().trim().isEmpty()) {
            loading(false)
            showToast("Enter Confirm Password")
            return
        } else if (inputPassword.text.toString() != inputConfirmPassword.text.toString()
        ) {
            loading(false)
            showToast("Password and confirm password must be same")
            return
        }
        else{
            Firebase.firestore.collection(CONSTANT.KEY_USER)
                .whereEqualTo(CONSTANT.KEY_USER_GMAIL, inputEmail.text.toString())
                .get()
                .addOnSuccessListener { value ->
                    if(value != null && !value.isEmpty){
                        loading(false)
                        callback.onInvalid()
                    }
                    else {
                        loading(false)
                        callback.onValid()
                    }
                }
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            buttonSignUp.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
            buttonSignUp.visibility = View.VISIBLE
        }
    }
    companion object {
        fun newInstance() : SignUpFragment{
            return SignUpFragment()
        }
    }
}