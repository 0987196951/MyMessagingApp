package com.example.mymessagingapp.Main.system.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.mymessagingapp.Main.utils.CONSTANT
import com.example.mymessagingapp.R
import com.example.mymessagingapp.Main.system.data.Group
import com.example.mymessagingapp.Main.system.data.User
import com.example.mymessagingapp.Main.system.interfaces.CallBackFromMakeGroup
import com.example.mymessagingapp.Main.system.Fragment.utilities.Inites
import java.io.FileNotFoundException
import java.util.*

private const val REQUEST_OK= 1
private const val TAG = "MakeGroupDialog"
class MakeGroupDialog : DialogFragment() {
    private lateinit var imageGroup : ImageView
    private lateinit var addImage : TextView
    private lateinit var enterNameGroup : EditText
    private var encodedImage : String? = null
    private lateinit var exit : Button
    private lateinit var accept : Button
    private lateinit var user : User
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.make_group_dialog, null)
            builder.setView(view)
            this.user = arguments?.getSerializable(CONSTANT.KEY_USER) as User
            imageGroup = view.findViewById(R.id.imageGroupAdded) as ImageView
            enterNameGroup = view.findViewById(R.id.enterNameGroup) as EditText
            exit = view.findViewById(R.id.exitMakeGroup) as Button
            accept = view.findViewById(R.id.acceptMakeGroup) as Button
            addImage = view.findViewById(R.id.textAddImageGroup) as TextView
            imageGroup.setOnClickListener {v ->
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivityForResult(intent, REQUEST_OK)
            }
            accept.setOnClickListener{ v ->
                if(checkValidMakeGroup()){
                    val group = Group(UUID.randomUUID().toString(),
                        enterNameGroup.text.toString(),
                        Date(),
                        true,
                        encodedImage?: Inites.encodeImage(BitmapFactory.decodeResource(resources, R.drawable.groupimage), 150)
                    )
                    targetFragment.let { fragment ->
                        (fragment as CallBackFromMakeGroup).onMadeGroup(group)
                    }
                    dialog?.dismiss()
                }
            }
            exit.setOnClickListener{
                dialog?.dismiss()
            }
            builder.create()
        }?: throw IllegalStateException("can't ")
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK)
            return
        if (requestCode == REQUEST_OK && data != null) {
            val imageUri = data!!.data
            try {
                val inputStream = requireActivity().contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageGroup.setImageBitmap(bitmap)
                encodedImage = Inites.encodeImage(bitmap, 777)
                Log.d(TAG, "encode Image is : " + encodedImage)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
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