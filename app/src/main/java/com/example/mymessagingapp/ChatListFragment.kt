package com.example.mymessagingapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messapp.R
import com.example.mymessagingapp.adapter.ConversationAdapter
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.interfaces.CallBackFromListUserFound
import com.example.mymessagingapp.modelview.ChatListViewModelFactory
import java.security.Provider
private val TAG = "ChatListFragment"
class ChatListFragment : Fragment(), CallBackFromListUserFound{
    private lateinit var imageUser : ImageView
    private lateinit var nameUser : TextView
    private lateinit var gmailUser : TextView
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
        findOtherUserButton = view.findViewById(R.id.findOtherUserButton) as Button
        findOtherUser = view.findViewById(R.id.chatListFindOtherUser) as EditText
        recyclerListConversation = view.findViewById(R.id.recyclerListConversation) as RecyclerView
        recyclerListConversation.layoutManager = LinearLayoutManager(context)
        recyclerListConversation.adapter = recyclerViewAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        factory.conversationAdapter.observe(
            viewLifecycleOwner,
            Observer { conversationAdapter ->
                conversationAdapter?.let {
                    Log.d(TAG, "made conversation adapter")
                    updateUI(conversationAdapter)
                }
            }
        )
    }
    private fun updateUI(conversationAdapter: ConversationAdapter){
        recyclerListConversation.adapter = conversationAdapter
    }
    override fun onStart() {
        super.onStart()
        findOtherUserButton.setOnClickListener {
            val s = findOtherUser.text.toString()

        }
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
        (requireActivity() as CallBackFromListUserFound).onUserFound(userFound)

    }
    private checkGroupIsExist(userFound : User){

    }
}