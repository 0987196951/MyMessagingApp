package com.example.mymessagingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.dialog.MakeGroupDialog
import com.example.mymessagingapp.interfaces.CallBackFromMakeGroup
import com.example.mymessagingapp.interfaces.CallBackWhenLogOut
import com.example.mymessagingapp.interfaces.CallBackWhenModifyDataUser

private val DIALOG_MAKE_GROUP = "Make New Group"
private val REQUEST_MAKE_GROUP = 1
private val REQUEST_ZOOM_PICTURE = 0
private val DIALOG_ZOOM_PICTURE = "Zoom picture"
class MoreInfoUser : Fragment(), CallBackFromMakeGroup{
    private lateinit var user : User
    private lateinit var imageUser : ImageView
    private lateinit var modifyInfoUser : TextView
    private lateinit var makeGroupButton : TextView
    private lateinit var emailUser : TextView
    private lateinit var logOut : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.user = arguments?.getSerializable(CONSTANT.KEY_USER) as User
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.more_info_user, container, false)
        imageUser = view.findViewById(R.id.imageUser) as ImageView
        modifyInfoUser = view.findViewById(R.id.modifyInfoUser) as TextView
        makeGroupButton = view.findViewById(R.id.makeGroup) as TextView
        logOut = view.findViewById(R.id.logOut) as TextView
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imageUser.setImageBitmap(getImage(user.image))
        imageUser.setOnClickListener {
            PicturePickerFragment.newInstance(user.image).apply {
                setTargetFragment(this@MoreInfoUser, REQUEST_ZOOM_PICTURE).apply {
                    show(this@MoreInfoUser.requireFragmentManager(), DIALOG_ZOOM_PICTURE)
                }
            }
        }
        makeGroupButton.setOnClickListener { v ->
            MakeGroupDialog.newInstance(user).apply {
                setTargetFragment(this@MoreInfoUser, REQUEST_MAKE_GROUP ).apply {
                    show(this@MoreInfoUser.requireFragmentManager(), DIALOG_MAKE_GROUP)
                }
            }
        }
        modifyInfoUser.setOnClickListener {
            (requireContext() as CallBackWhenModifyDataUser).onModify(user)
        }
        logOut.setOnClickListener {
            (requireContext() as CallBackWhenLogOut).onLogout()
        }
    }
    private fun getImage(encodeImage : String) : Bitmap {
        val bytes = Base64.decode(encodeImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    companion object {
        fun newInstance(user : User) : MoreInfoUser {
            var args = Bundle().apply {
                putSerializable(CONSTANT.KEY_USER, user)
            }
            return MoreInfoUser().apply {
                arguments = args
            }
        }
    }
    override fun onMadeGroup(groupMade: Group) {
        (requireContext() as CallBackFromMakeGroup).onMadeGroup(groupMade)
    }
}