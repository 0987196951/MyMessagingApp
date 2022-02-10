package com.example.mymessagingapp.Main.system.Fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.mymessagingapp.Main.system.data.User
import com.example.mymessagingapp.Main.system.Fragment.utilities.Inites
import com.example.mymessagingapp.Main.utils.CONSTANT
import com.example.mymessagingapp.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.FileNotFoundException
import java.text.ParseException

private const val RESULT_OK = 1
private const val TAG = "ModifyInfoUserFragment"
class ModifyInfoUserFragment : Fragment() {
    private lateinit var user : User
    private var encodedImage: String? = null
    private lateinit var imageProfile : ImageView
    private lateinit var inputName : EditText
    private lateinit var inputDate : EditText
    private lateinit var inputEmail : TextView
    private lateinit var inputPassword : EditText
    private lateinit var inputConfirmPassword : EditText
    private lateinit var acceptModifyButton : Button
    private lateinit var progressBar : ProgressBar
    private lateinit var dayCreatedAccount : TextView
    private var isChangePassword : Boolean = false
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
        inputEmail = view.findViewById(R.id.inputEmail) as TextView
        inputPassword = view.findViewById(R.id.inputPassword) as EditText
        inputConfirmPassword = view.findViewById(R.id.inputConfirmPassword) as EditText
        progressBar = view.findViewById(R.id.progressBar) as ProgressBar
        acceptModifyButton = view.findViewById(R.id.accept_modify) as Button
        dayCreatedAccount = view.findViewById(R.id.dayCreatedAccount) as TextView
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        encodedImage = user.image
        imageProfile.setImageBitmap(getImage(user.image))
        inputName.setText(user.name, TextView.BufferType.EDITABLE)
        inputDate.setText(Inites.parseDateToString(user.dateOfBirth), TextView.BufferType.EDITABLE)
        dayCreatedAccount.text = Inites.parseDateToString(user.accountCreate)
        inputEmail.text = user.gmail
        inputPassword.setText(user.password)
        inputConfirmPassword.visibility = View.GONE
        inputPassword.setOnClickListener {
            inputConfirmPassword.visibility = View.VISIBLE
        }
        imageProfile.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, RESULT_OK)
        }
        acceptModifyButton.setOnClickListener {
            loading(true)
            if(checkInvalid(isChangePassword)){
                try {
                    val date = Inites.parseDateFromString(inputDate.text.toString().trim())
                    val hashMapChange = mapOf(
                        CONSTANT.KEY_USER_NAME to inputName.text.toString().trim(),
                        CONSTANT.KEY_USER_DATE_OF_BIRTH to date,
                        CONSTANT.KEY_USER_IMAGE to encodedImage,
                        CONSTANT.KEY_USER_PASSWORD to inputPassword.text.toString().trim()
                    )
                    Firebase.firestore.collection(CONSTANT.KEY_USER).document(user.userId).update(hashMapChange)
                    showToast("modify data user success")
                }catch (e : ParseException){
                    showToast("date input must to pattern dd/MM/yyyy")
                }
            }
        }
    }
    private fun showToast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    fun checkInvalid(isClickPassword : Boolean) : Boolean {
        loading(false)
        if (inputName.text.toString().trim().isEmpty()) {
            showToast("Enter Name")
            return false
        }else if(inputName.text.toString().trim() == "admin"){
            showToast("can't make name is 'admin")
            return false
        }
        else if (inputDate.text.toString().trim().isEmpty()) {
            showToast("Enter Date of Birth")
            return false
        }
        if(isClickPassword){
            if (inputPassword.text.toString().trim().isEmpty()) {
                showToast("Enter Password")
                return false
            }
            else if(inputConfirmPassword.text.toString().trim().isEmpty()){
                showToast("Enter confirm password")
                return false
            }
            else if(inputConfirmPassword.toString().trim() != inputPassword.toString().trim()){
                showToast("confirm password must same password")
                return false
            }
        }
        loading(false)
        return true
    }
    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            acceptModifyButton.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
            acceptModifyButton.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK)
            return
        Log.d(TAG, "alo alo")
        if (requestCode == RESULT_OK && data != null) {
            val imageUri = data!!.data
            try {
                val inputStream = requireActivity().contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageProfile.setImageBitmap(bitmap)
                encodedImage = Inites.encodeImage(bitmap, 777)
                user.image = encodedImage as String
                Log.d(TAG, "encode Image is : " + encodedImage)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }
    private fun getImage(encodeImage : String) : Bitmap {
        val bytes = Base64.decode(encodeImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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
}
