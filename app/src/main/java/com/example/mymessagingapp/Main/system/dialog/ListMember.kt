package com.example.mymessagingapp.Main.system.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymessagingapp.Main.utils.CONSTANT
import com.example.mymessagingapp.R
import com.example.mymessagingapp.Main.system.data.Group
import com.example.mymessagingapp.Main.system.data.User
import com.example.mymessagingapp.Main.system.interfaces.CallBackWhenSelectOtherUserInGroup
import com.example.mymessagingapp.Main.system.modelview.ListMemBerViewModelFactory
import com.example.mymessagingapp.Main.system.Fragment.utilities.Inites
import java.lang.IllegalStateException

class ListMember : DialogFragment() {
    private lateinit var group : Group
    private lateinit var recyclerView: RecyclerView
    private var listMemberViewModel : ListMemBerViewModelFactory? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.list_member, null)
            builder.setView(view)
            builder.setTitle("List Member in Group")
            group = arguments?.getSerializable(CONSTANT.KEY_GROUP) as Group
            recyclerView = view.findViewById(R.id.listMemberRecyclerView) as RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)
            if(listMemberViewModel == null) {
                listMemberViewModel = ListMemBerViewModelFactory(group.groupId)
            }
            listMemberViewModel!!.listMemberLiveData
                .observe(this,
                    Observer { users ->
                        updateUI(users)
                    }
                )
            builder.create()

        }?: throw IllegalStateException("Can't display list Member ")
    }

    private fun updateUI(users: MutableList<User>?) {
        val adapter = OtherUserAdapter(users!!)
        recyclerView.adapter = adapter
    }
    private inner class OtherViewHolder(view : View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var user : User
        private val imageOtherUser = view.findViewById(R.id.imageUserFound) as ImageView
        private val nameOtherFound = view.findViewById(R.id.nameUserFound) as TextView
        fun bind(user : User){
            this.user = user
            imageOtherUser.setImageBitmap(Inites.getImage(user.image))
            nameOtherFound.text = user.name
        }
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(view : View?) {
            targetFragment.let {    fragment ->
                (fragment as CallBackWhenSelectOtherUserInGroup).onUserSelect(user)
            }
            dismiss()
        }
    }
    private inner class OtherUserAdapter (val listMember : List<User>)
        : RecyclerView.Adapter<OtherViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherViewHolder {
            val view = layoutInflater.inflate(R.layout.found_users, parent, false)
            return OtherViewHolder(view)
        }

        override fun onBindViewHolder(holder: OtherViewHolder, position: Int) {
            return holder.bind(listMember[position])
        }

        override fun getItemCount(): Int {
            return listMember.size
        }

    }
    companion object {
        fun newInstance(group : Group) : ListMember {
            val args = Bundle().apply {
                putSerializable(CONSTANT.KEY_GROUP, group)
            }
            return ListMember().apply {
                arguments = args
            }
        }
    }
}