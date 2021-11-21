package com.example.mymessagingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.dialog.MakeGroupDialog

private val DIALOG_MAKE_GROUP = "Make New Group"
private val REQUEST_MAKE_GROUP = 1

class MoreInfoUser : Fragment(){
    private lateinit var user : User
    private lateinit var imageUser : ImageView
    private lateinit var modifyInfoUser : TextView
    private lateinit var makeGroupButton : TextView
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
        makeGroupButton.setOnClickListener { v ->
            MakeGroupDialog.newInstance(user).apply {
                setTargetFragment(this@MoreInfoUser, REQUEST_MAKE_GROUP ).apply {
                    show(this@MoreInfoUser.requireFragmentManager(), DIALOG_MAKE_GROUP)
                }
            }
        }

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
}