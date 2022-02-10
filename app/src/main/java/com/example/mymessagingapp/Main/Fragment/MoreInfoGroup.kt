package com.example.mymessagingapp.Main.Fragment

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mymessagingapp.Main.data.Group
import com.example.mymessagingapp.Main.data.User
import com.example.mymessagingapp.Main.dialog.ListMember
import com.example.mymessagingapp.Main.dialog.ListUserFoundDialog
import com.example.mymessagingapp.Main.interfaces.CallBackAddUserToGroup
import com.example.mymessagingapp.Main.interfaces.CallBackFromListUserFound
import com.example.mymessagingapp.Main.interfaces.CallBackWhenOutGroup
import com.example.mymessagingapp.Main.interfaces.CallBackWhenSelectOtherUserInGroup
import com.example.mymessagingapp.Main.Fragment.utilities.Inites
import com.example.mymessagingapp.Main.utilities.CONSTANT
import com.example.mymessagingapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.FileNotFoundException
import java.util.*

private const val REQUEST_SEE_LIST_MEMBER = 0
private const val DIALOG_SEE_LIST_MEMBER = "List Member"
private const val REQUEST_FIND_OTHER_USER = 1
private const val DIALOG_FIND_OTHER_USER = "Find Other User"
private const val RESULT_OK = 1
private const val TAG = "MoreInfoGroup"
class MoreInfoGroup : Fragment(), CallBackWhenSelectOtherUserInGroup, CallBackAddUserToGroup {
    private lateinit var group : Group
    private lateinit var user : User
    private lateinit var imageGroup : ImageView
    private lateinit var nameGroup : EditText
    private lateinit var acceptChange : Button
    private lateinit var listMember : TextView
    private lateinit var addMember : TextView
    private lateinit var outGroup : TextView
    private var encodeImage : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        group = arguments?.getSerializable(CONSTANT.KEY_GROUP) as Group
        user = arguments?.getSerializable(CONSTANT.KEY_USER) as User
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.more_info_group, container, false)
        imageGroup = view.findViewById(R.id.imageGroup) as ImageView
        nameGroup = view.findViewById(R.id.nameGroup) as EditText
        acceptChange = view.findViewById(R.id.acceptChangeNameGroup) as Button
        listMember = view.findViewById(R.id.seeListMember) as TextView
        addMember = view.findViewById(R.id.addMember) as TextView
        outGroup = view.findViewById(R.id.outGroup) as TextView
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageGroup.setImageBitmap(Inites.getImage(group.imageGroup))
        imageGroup.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, RESULT_OK)
        }
        listMember.setOnClickListener{ v ->
            ListMember.newInstance(group).apply{
                setTargetFragment(this@MoreInfoGroup, REQUEST_SEE_LIST_MEMBER).apply {
                    show(this@MoreInfoGroup.requireFragmentManager(), DIALOG_SEE_LIST_MEMBER)
                }
            }
        }
        addMember.setOnClickListener { v ->
            ListUserFoundDialog.newInstance("").apply {
                setTargetFragment(this@MoreInfoGroup, REQUEST_FIND_OTHER_USER).apply {
                    show(this@MoreInfoGroup.requireFragmentManager(), DIALOG_FIND_OTHER_USER)
                }
            }
        }
        outGroup.setOnClickListener { v->
            MaterialAlertDialogBuilder(requireContext())
                .setMessage("Do you want to leave here ??")
                .setPositiveButton("Accept"){ dialog, which ->
                    Inites.makeNewMessageOfSystem("${user.name} is outed group", group.groupId)
                    (requireContext() as CallBackWhenOutGroup).outGroup(group)
                }
                .setNegativeButton("Cancel"){ dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
        acceptChange.setOnClickListener { v->
            if(nameGroup.text.isNotBlank()){
                v.visibility = View.INVISIBLE
                Firebase.firestore.collection(CONSTANT.KEY_GROUP).document(group.groupId)
                    .update(mapOf(
                        CONSTANT.KEY_GROUP_NAME to nameGroup.text.toString().trim()
                    ))
                    .continueWith {
                        v.visibility = View.VISIBLE
                    }
            }
        }
    }
    companion object {
        fun newInstance(group : Group, user: User) : MoreInfoGroup{
            val args = Bundle().apply {
                putSerializable(CONSTANT.KEY_GROUP, group)
                putSerializable(CONSTANT.KEY_USER, user)
            }
            return MoreInfoGroup().apply {
                arguments = args
            }
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
                imageGroup.setImageBitmap(bitmap)
                encodeImage = Inites.encodeImage(bitmap, 777)
                group.imageGroup = encodeImage as String
                Firebase.firestore.collection(CONSTANT.KEY_GROUP).document(group.groupId)
                    .update(mapOf(
                        CONSTANT.KEY_GROUP_IMAGE to encodeImage
                    ))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }
    override fun onUserSelect(user: User) {
        requireActivity().supportFragmentManager.popBackStack()
        requireActivity().supportFragmentManager.popBackStack()
        (requireActivity() as CallBackFromListUserFound).onUserFound(user)
    }
    override fun onAddOtherUserToGroup(userAdded: User) {
        val db = Firebase.firestore
        db.collection(CONSTANT.KEY_GROUP).document(group.groupId)
            .get()
            .addOnSuccessListener { value ->
                if(value != null){
                    var check = true
                    val listMember = value.data?.get(CONSTANT.KEY_GROUP_LIST_MEMBER) as List<String>
                    for (i in listMember){
                        if(i == userAdded.userId){
                            check = false
                            break
                        }
                    }
                    if(check == true){
                        val hashNewMessage = mapOf(
                            CONSTANT.KEY_MESSAGE_SENDER_NAME to "system",
                            CONSTANT.KEY_MESSAGE_CONTENT to "${user.name} is added ${userAdded.name} into this group",
                            CONSTANT.KEY_MESSAGE_TIME_SEND to Date(),
                            CONSTANT.KEY_MESSAGE_SENDER_ID to CONSTANT.KEY_MESSAGE_SYSTEM_ID
                        )
                        db.collection(CONSTANT.KEY_GROUP).document(group.groupId)
                            .update(CONSTANT.KEY_GROUP_LIST_MEMBER, FieldValue.arrayUnion(userAdded.userId))
                        db.collection(CONSTANT.KEY_GROUP).document(group.groupId)
                            .collection(CONSTANT.KEY_MESSAGE).add(hashNewMessage)
                        db.collection(CONSTANT.KEY_USER_LIST_GROUP_ID).document(user.userId)
                            .update(CONSTANT.KEY_USER_LIST_GROUP_ID, FieldValue.arrayUnion(group.groupId))
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage("you added ${userAdded.name} into this group")
                            .show()
                    }
                    else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage("${userAdded.name} are in this group")
                            .show()
                    }
                }
            }
    }
}