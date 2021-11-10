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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymessagingapp.adapter.ConversationAdapter
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.dialog.MakeGroupDialog
import com.example.mymessagingapp.interfaces.CallBackFromChatList
import com.example.mymessagingapp.interfaces.CallBackFromListUserFound
import com.example.mymessagingapp.interfaces.CallBackFromMakeGroup
import com.example.mymessagingapp.interfaces.CallBackWhenGroupExisted
import com.example.mymessagingapp.modelview.ChatListViewModelFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
private val DIALOG_MAKE_GROUP = "Make New Group"
private val REQUEST_MAKE_GROUP = 1
private val TAG = "ChatListFragment"
class ChatListFragment : Fragment(), CallBackFromListUserFound, CallBackFromMakeGroup, CallBackFromChatList{
    private lateinit var imageUser : ImageView
    private lateinit var nameUser : TextView
    private lateinit var gmailUser : TextView
    private lateinit var addGroup: Button
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
        addGroup = view.findViewById(R.id.addGroup) as Button
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
        addGroup.setOnClickListener{
            MakeGroupDialog.newInstance(user).apply {
                setTargetFragment(this@ChatListFragment, REQUEST_MAKE_GROUP ).apply {
                    show(this@ChatListFragment.requireFragmentManager(), DIALOG_MAKE_GROUP)
                }
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
                    Log.d(TAG, "made conversation adapter")
                    updateUI(it)
                }
            }
        )
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
        var s = checkGroupIsExist(userFound)
        if(s == null)
            (requireActivity() as CallBackFromListUserFound).onUserFound(userFound)
        else {
            Firebase.firestore.collection(CONSTANT.KEY_GROUP).document(s).get()
                .addOnSuccessListener { value ->
                    (requireActivity() as CallBackWhenGroupExisted).onGroupExist(
                        Group(s,
                            value.data?.get("nameGroup") as String,
                            value.data!!["createdGroup"] as Date,
                            value.data!!["isGroup"] as Boolean,
                            value.data!!["imageGroup"] as String
                        )
                    )
                }
        }
    }
    override fun onMadeGroup(groupMade: Group) {
        (requireActivity() as CallBackFromMakeGroup).onMadeGroup(groupMade)
    }
    private fun checkGroupIsExist(userFound : User) : String? {
        var listGroupId : List<String> = emptyList()
        Firebase.firestore.collection(CONSTANT.KEY_USER).document(user.userId).get()
            .addOnSuccessListener { value ->
                listGroupId = value.data?.get(CONSTANT.KEY_GROUP_ID) as List<String>
            }
        var groupIdCanFind : String? = null
        Firebase.firestore.collection(CONSTANT.KEY_GROUP)
            .whereEqualTo("isGroup", true)
            .whereIn(CONSTANT.KEY_GROUP_ID, listGroupId)
            .get().addOnSuccessListener { value ->
                    if(value != null) {
                        for(doc in value){
                            groupIdCanFind = doc.data.get("groupId") as String
                        }
                    }
            }
        return groupIdCanFind
    }

    override fun onGroupSelected(groupId: String) {
        (context as CallBackFromChatList).onGroupSelected(groupId)
    }

}