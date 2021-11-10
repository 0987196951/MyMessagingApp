package com.example.mymessagingapp.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.example.mymessagingapp.R
import com.example.mymessagingapp.CONSTANT
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.interfaces.CallBackFromMakeGroup
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.*

private val REQUEST_CODE_IMAGE = 1
private val TAG = "MakeGroupDialog"
class MakeGroupDialog : DialogFragment() {
    private lateinit var imageGroup : ImageView
    private lateinit var enterNameGroup : EditText
    private var encodedImage : String? = null
    private lateinit var exit : Button
    private lateinit var accept : Button
    private lateinit var user :User
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.make_group_dialog, null)
            builder.setView(view)
            this.user = arguments?.getSerializable(CONSTANT.KEY_USER) as User
            imageGroup = view.findViewById(R.id.imageGroupAdded) as ImageView
            imageGroup.setOnClickListener {v ->
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
               pickImage.launch(intent)
            }
            enterNameGroup = view.findViewById(R.id.enterNameGroup) as EditText
            exit = view.findViewById(R.id.exitMakeGroup) as Button
            accept = view.findViewById(R.id.acceptMakeGroup) as Button
            accept.setOnClickListener{ v ->
                if(checkValidMakeGroup()){
                    val group = Group(UUID.randomUUID().toString(),
                        enterNameGroup.text.toString(),
                        Date(),
                        true,
                        encodedImage?:CONSTANT.IMAGE_DEFAULT
                    )
                    val newGroup = hashMapOf(
                        "groupId" to group.groupId,
                        "nameGroup" to group.nameGroup,
                        "createdGroup" to group.createdGroup,
                        "isGroup" to true,
                        "imageGroup" to group.imageGroup,
                        "conversation" to mapOf(
                            "senderId" to "",
                            "content" to "",
                            "timeSend" to ""
                        )
                    )
                    Firebase.firestore.collection(CONSTANT.KEY_GROUP)
                        .document(group.groupId).set(newGroup).addOnSuccessListener {
                            Log.d(TAG, "Can up group to firebase")
                        }.addOnFailureListener{ e->
                            Log.d(TAG, "can't up group into firestore")
                        }
                    targetFragment.let { fragment ->
                        (fragment as CallBackFromMakeGroup).onMadeGroup(group)
                    }
                    /*Firebase.firestore.collection(CONSTANT.KEY_USER)
                        .document(user.userId).update(mapOf(
                            "member_list_id" to user.group_list_id.plus(group.groupId)
                        ))*/
                    dialog?.dismiss()
                }
            }
            builder.create()
        }?: throw IllegalStateException("can't ")
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
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            if (result.data != null) {
                val imageUri = result.data!!.data
                try {
                    val inputStream = context?.contentResolver?.openInputStream(imageUri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageGroup.setImageBitmap(bitmap)
                    encodedImage = encodeImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun checkValidMakeGroup() : Boolean {
        if(enterNameGroup.text.isEmpty()) {
            Toast.makeText(context, "Enter name of Group", Toast.LENGTH_SHORT)
            return false
        }
        return true
    }
    companion object {
        fun newInstance(user: User) : MakeGroupDialog{
            var args = Bundle().apply {
                putSerializable(CONSTANT.KEY_USER, user)
            }
            return MakeGroupDialog().apply {
                arguments = args
            }
        }
    }
}