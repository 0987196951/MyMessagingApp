package com.example.mymessagingapp

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.interfaces.CallBackWhenLoginSuccess
import com.example.mymessagingapp.interfaces.CallBackWhenSignUp
import com.example.mymessagingapp.utilities.Inites
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private val RESULT_OK = 1
private val TAG = "SignInFragment"
class SignInFragment : Fragment(){
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword : EditText
    private lateinit var buttonSignIn: Button
    private lateinit var textCreateAccount : TextView
    private lateinit var progressBar : ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_sign_in, container, false)
        inputEmail = view.findViewById(R.id.inputEmail) as EditText
        inputPassword = view.findViewById(R.id.inputPassword) as EditText
        buttonSignIn = view.findViewById(R.id.buttonSignIn) as Button
        textCreateAccount = view.findViewById(R.id.textCreateNewAccount) as TextView
        progressBar = view.findViewById(R.id.progressBar) as ProgressBar
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListeners()
    }

    private fun setListeners(){
        textCreateAccount.setOnClickListener { v ->
            (requireContext() as CallBackWhenSignUp).onSignUp()
        }
        buttonSignIn.setOnClickListener { v ->
            if (isValidSignUpDetails()) {
                signIn()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun signIn(){
        loading(true)
        val inputEmailA = inputEmail.text.toString().trim()
        val inputPasswordA = inputPassword.text.toString().trim()
        val database = Firebase.firestore
        database.collection(CONSTANT.KEY_USER)
            .whereEqualTo(CONSTANT.KEY_USER_GMAIL, inputEmailA)
            .whereEqualTo(CONSTANT.KEY_USER_PASSWORD, inputPasswordA)
            .get()
            .addOnSuccessListener { value ->
                if(value != null && !value.isEmpty){
                    for (doc in value){
                        val user = User(doc.data.get(CONSTANT.KEY_USER_ID) as String,
                        doc.data.get(CONSTANT.KEY_USER_NAME) as String,
                            inputPasswordA,
                            inputEmailA,
                            Inites.convertTimeStampToDate(doc.data.get(CONSTANT.KEY_USER_DATE_OF_BIRTH) as Timestamp),
                            Inites.convertTimeStampToDate(doc.data.get(CONSTANT.KEY_USER_CREATE_ACCOUNT) as Timestamp),
                            doc.data.get(CONSTANT.KEY_USER_IMAGE) as String,
                            true
                        )
                        writeData(inputEmailA, inputPasswordA)
                        showToast("Login success")
                        (requireContext() as CallBackWhenLoginSuccess).onLoginSuccess(user, 1)
                    }
                    loading(false)
                }
                else {
                    loading(false)
                    showToast("Email or password is invalid")
                }
            }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            buttonSignIn.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
            buttonSignIn.visibility = View.VISIBLE
        }
    }

    private fun isValidSignUpDetails() : Boolean{
        if (inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter Email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.text.toString()).matches()) {
            showToast("Enter valid Email")
            return false
        } else if (inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter Password")
            return false
        }
        return true
    }
    companion object {
        fun newInstance() : SignInFragment{
            return SignInFragment()
        }
    }
    fun writeData(email : String , password : String ){
        val fileName = "myaccount.txt"
        context?.openFileOutput(fileName, Context.MODE_PRIVATE).use{
            it?.write("${email}\n".toByteArray())
            it?.write("${password}".toByteArray())
        }
    }
}