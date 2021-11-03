package com.example.mymessagingapp.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messapp.R
import com.example.mymessagingapp.CONSTANT
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.interfaces.CallBackFromListUserFound
import com.example.mymessagingapp.modelview.FindOtherUserViewModel
import java.lang.IllegalStateException

class ListUserFoundDialog : DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteCanNotFind : TextView
    private val key_find = arguments?.getSerializable(CONSTANT.KEY_FIND_OTHER_USER) as String
    val listOtherUserViewModel : FindOtherUserViewModel by lazy {
        FindOtherUserViewModel(key_find)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater =requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.find_other_user, null)
            builder.setView(view)
            noteCanNotFind = view.findViewById(R.id.canNotOtherUser) as TextView
            recyclerView = view.findViewById(R.id.findOtherUserRecyclerview) as RecyclerView
            recyclerView.adapter = OtherUserAdapter(listOtherUserViewModel.getListOtherUser())
            recyclerView.layoutManager = LinearLayoutManager(context)
            if(listOtherUserViewModel.getListOtherUser().size == 0 ) {
                noteCanNotFind.visibility = View.GONE
            }
            else {
                noteCanNotFind.visibility = View.VISIBLE
            }
            builder.create()
        }?: throw IllegalStateException("Activity other user can't find")
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
                (fragment as CallBackFromListUserFound).onUserFound(user)
            }
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
            val arg = Bundle().apply {
                putSerializable(CONSTANT.KEY_FIND_OTHER_USER, key_find)
            }
            return ListUserFoundDialog().apply {
                arguments = arg
            }
        }
    }
    private fun getImage(encodeImage : String) : Bitmap {
        val bytes = Base64.decode(encodeImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}