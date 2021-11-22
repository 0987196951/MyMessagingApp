package com.example.mymessagingapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymessagingapp.adapter.ConversationAdapter
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.dialog.ListUserFoundDialog
import com.example.mymessagingapp.dialog.MakeGroupDialog
import com.example.mymessagingapp.interfaces.*
import com.example.mymessagingapp.modelview.ChatListViewModelFactory
import com.example.mymessagingapp.utilities.Inites
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
private val DIALOG_MAKE_GROUP = "Make New Group"
private val DIALOG_FIND_OTHER_USER = "Find other User"
private val REQUEST_MAKE_GROUP = 1
private val REQUEST_FIND_OTHER_USER = 2
private val TAG = "ChatListFragment"
class ChatListFragment : Fragment(), CallBackFromListUserFound{
    private lateinit var imageUser : ImageView
    private lateinit var nameUser : TextView
    private lateinit var gmailUser : TextView
    private lateinit var moreInfo: ImageButton
    private lateinit var findOtherUserButton : Button
    private lateinit var recyclerListConversation: RecyclerView
    private lateinit var recyclerViewAdapter: ConversationAdapter
    private lateinit var user : User
    private lateinit var findOtherUser : EditText
    private val factory : ChatListViewModelFactory by lazy {
        ChatListViewModelFactory(user, requireContext(), layoutInflater)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = arguments?.getSerializable(CONSTANT.KEY_USER) as User
        recyclerViewAdapter = factory.conversationAdapter.value!!
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chat_list_fragment, container, false)
        imageUser = view.findViewById(R.id.chatListImageUser) as ImageView
        nameUser = view.findViewById((R.id.chatListUserName)) as TextView
        gmailUser = view.findViewById(R.id.chatListUserGmail) as TextView
        moreInfo = view.findViewById(R.id.InfoUser) as ImageButton
        findOtherUserButton = view.findViewById(R.id.findOtherUserButton) as Button
        findOtherUser = view.findViewById(R.id.chatListFindOtherUser) as EditText
        recyclerListConversation = view.findViewById(R.id.recyclerListConversation) as RecyclerView
        recyclerListConversation.layoutManager = LinearLayoutManager(context)
        recyclerListConversation.adapter = recyclerViewAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameUser.text = user.name
        gmailUser.text = user.gmail
        imageUser.setImageBitmap(Inites.getImage(user.image))
        moreInfo.setOnClickListener{
            (requireActivity() as CallBackWhenSeeInfoUser).onSeeInfoUser()
        }
        findOtherUserButton.setOnClickListener {
            Log.d(TAG, "find other User with ${findOtherUser.text.toString()}")
            if(!findOtherUser.text.isEmpty()){
                ListUserFoundDialog.newInstance(findOtherUser.text.toString()).apply {
                    setTargetFragment(this@ChatListFragment, REQUEST_FIND_OTHER_USER).apply {
                        show(this@ChatListFragment.requireFragmentManager(), DIALOG_FIND_OTHER_USER)
                    }
                }
                findOtherUser.text.clear()
            }
        }
    }
    private fun updateUI(conversationAdapter: ConversationAdapter){
        recyclerListConversation.adapter = conversationAdapter
    }
    override fun onStart() {
        super.onStart()
        factory.conversationAdapter.observe(
            viewLifecycleOwner,
            Observer { conversationAdapter ->
                conversationAdapter?.let {
                    Log.d(TAG, "size of list conversation is ${conversationAdapter.itemCount}")
                    updateUI(it)
                }
            }
        )
    }
    companion object {
        fun newInstance(user : User): ChatListFragment {
            val args = Bundle().apply{
                putSerializable(CONSTANT.KEY_USER, user)
            }
            return ChatListFragment().apply {
                arguments = args
            }
        }
    }

    override fun onUserFound(userFound: User) {
        Log.d(TAG, "onUserFound in ChatListFragment")
        (requireContext() as CallBackFromListUserFound).onUserFound(userFound)
    }

}