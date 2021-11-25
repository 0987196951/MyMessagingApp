package com.example.mymessagingapp.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymessagingapp.*
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.interfaces.CallBackAddUserToGroup
import com.example.mymessagingapp.interfaces.CallBackFromListUserFound
import com.example.mymessagingapp.modelview.FindOtherUserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.IllegalStateException
private val TAG = "ListUserFoundDialog"
class ListUserFoundDialog : DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteCanNotFind : TextView
    private lateinit var keyFind : String
    private lateinit var findOtherUserButton: Button
    private lateinit var textFindOther : EditText
    private var listOtherUserViewModel : FindOtherUserViewModel? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.find_other_user, null)
            builder.setView(view)
            builder.setTitle("Find other user")
            keyFind = arguments?.getString(CONSTANT.KEY_FIND_OTHER_USER) as String
            if(listOtherUserViewModel == null){
                listOtherUserViewModel = FindOtherUserViewModel(keyFind)
            }
            noteCanNotFind = view.findViewById(R.id.canNotOtherUser) as TextView
            recyclerView = view.findViewById(R.id.findOtherUserRecyclerview) as RecyclerView
            findOtherUserButton = view.findViewById(R.id.findOtherUserButtonInDialog) as Button
            textFindOther = view.findViewById(R.id.findOtherUserEditTextInDialog) as EditText
            recyclerView.layoutManager = LinearLayoutManager(context)
            findOtherUserButton.setOnClickListener { v->
                if(textFindOther.text.isNotEmpty()){
                    listOtherUserViewModel!!.remakeListOtherUser(textFindOther.text.toString())
                }
                textFindOther.text.clear()
            }
            listOtherUserViewModel!!.listOtherUser.observe(
                this,
                Observer { users ->
                    updateUI(users)
                }
            )
            builder.create()
        }?: throw IllegalStateException("Activity other user can't find")
    }
    private fun updateUI(users: MutableList<User>) {
        val adapter = OtherUserAdapter(users)
        recyclerView.adapter = adapter
        if(users.size == 0 ) {
            noteCanNotFind.visibility = View.VISIBLE
        }
        else {
            noteCanNotFind.visibility = View.GONE
        }
    }

    private inner class OtherViewHolder(view : View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var user : User
        private val imageOtherUser = view.findViewById(R.id.imageUserFound) as ImageView
        private val nameOtherFound = view.findViewById(R.id.nameUserFound) as TextView
        fun bind(user : User){
            this.user = user
            imageOtherUser.setImageBitmap(getImage(user.image))
            nameOtherFound.text = user.name
        }
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(view : View?) {
            targetFragment.let {    fragment ->
                if(fragment is ChatListFragment){
                        Log.d(TAG, "fragment adjust called is Chat List Fragment")
                        (fragment as CallBackFromListUserFound).onUserFound(user)
                }
                else if(fragment is MoreInfoGroup) {
                    Log.d(TAG, "fragment adjust called is Chat Fragment")
                    MaterialAlertDialogBuilder(context!!)
                        .setMessage("Are you sure add ${user.name} into this group")
                        .setPositiveButton("Accept"){ dialog, which ->
                            (fragment as CallBackAddUserToGroup).onAddOtherUserToGroup(user)
                        }
                        .setNegativeButton("No"){ dialog, which ->
                            dismiss()
                        }.show()
                }
            }
            dismiss()
        }
    }
    private inner class OtherUserAdapter (val listOtherUser : List<User>)
        : RecyclerView.Adapter<OtherViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherViewHolder {
            val view = layoutInflater.inflate(R.layout.found_users, parent, false)
            return OtherViewHolder(view)
        }

        override fun onBindViewHolder(holder: OtherViewHolder, position: Int) {
            return holder.bind(listOtherUser[position])
        }

        override fun getItemCount(): Int {
            return listOtherUser.size
        }

    }
    companion object {
        fun newInstance(key_find : String ) : ListUserFoundDialog {
            Log.d(TAG, "Key Find other user : $key_find")
            val args = Bundle().apply {
                putSerializable(CONSTANT.KEY_FIND_OTHER_USER, key_find)
            }
            return ListUserFoundDialog().apply {
                arguments = args
            }
        }
    }
    private fun getImage(encodeImage : String) : Bitmap {
        val bytes = Base64.decode(encodeImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}