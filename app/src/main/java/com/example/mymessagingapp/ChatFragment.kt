package com.example.mymessagingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.example.mymessagingapp.data.ChatMessage
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.example.mymessagingapp.dialog.ListUserFoundDialog
import com.example.mymessagingapp.interfaces.CallBackAddUserToGroup
import com.example.mymessagingapp.modelview.ChatViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.concurrent.Executors
private val TAG = "ChatFragmentListener"
private val REQUEST_FIND_OTHER_USER = 1
private val DIALOG_FIND_OTHER_USER = "DialogFindOtherUser"
class ChatFragment : Fragment(), CallBackAddUserToGroup {
    private lateinit var user : User
    private lateinit var group : Group 
    private lateinit var nameReceiver : TextView
    private lateinit var imageReceiver : ImageView
    private lateinit var addOtherButton : Button
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var sendingMessage : EditText
    private lateinit var sendingMessageButton : Button
    private lateinit var adapter : ChatRecyclerAdapter
    private val chatViewModel by lazy {
        ChatViewModelFactory(user, group)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = arguments?.getSerializable(CONSTANT.KEY_USER) as User
        group = arguments?.getSerializable(CONSTANT.KEY_GROUP) as Group
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chat_message, container, false)
        nameReceiver = view.findViewById(R.id.nameReceiverChatMessage) as TextView
        imageReceiver = view.findViewById(R.id.imageReceiverChatMessage) as ImageView
        addOtherButton = view.findViewById(R.id.addOtherUser) as Button
        messageRecyclerView = view.findViewById(R.id.chatListRecyclerView) as RecyclerView
        var linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = false
        linearLayoutManager.stackFromEnd = true
        messageRecyclerView.layoutManager = linearLayoutManager
        sendingMessage = view.findViewById(R.id.sendMessage) as EditText
        sendingMessageButton = view.findViewById(R.id.sendMessageButton) as Button
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameReceiver.text = group.nameGroup
        //imageReceiver.setImageBitmap(getImage(group.imageGroup))
        adapter = chatViewModel.listMessage.value?.let { ChatRecyclerAdapter(it) }!!
        chatViewModel.listMessage.observe(
            viewLifecycleOwner,
            Observer { messes ->
                messes?.let {
                    Log.d("ChatFragment", "size of listMessage is ${messes.size}")
                    adapter.submitList(it)
                    messageRecyclerView.adapter = adapter
                }
            })

    }

    override fun onStart() {
        super.onStart()
        /*val sendPlace = object :TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
                val s = sequence.toString()
                addNewConversation(s)
                addNewMessage(s)
            }

            override fun afterTextChanged(p0: Editable?) {
                TODO("Not yet implemented")
            }

        }
        sendingMessage.addTextChangedListener(sendPlace)*/
        sendingMessageButton.setOnClickListener { v ->
            val s = sendingMessage.text.toString()
            sendingMessage.text.clear()
            addNewMessage(s)
        }
        addOtherButton.setOnClickListener { v ->
            ListUserFoundDialog.newInstance("").apply {
                setTargetFragment(this@ChatFragment, REQUEST_FIND_OTHER_USER).apply {
                    show(this@ChatFragment.requireFragmentManager(), DIALOG_FIND_OTHER_USER)
                }
            }
        }
    }
    private inner class ChatHolder(var view : View) : RecyclerView.ViewHolder(view) {
        private lateinit var message : ChatMessage
        private lateinit var imageMessage : ImageView
        private lateinit var contentMessage : TextView
        private lateinit var timeMessage : TextView
        fun bind(chat: ChatMessage, viewType : Int){
            this.message = chat
            if(viewType == CONSTANT.VIEW_TYPE_MESSAGE_SYSTEM){
                contentMessage = view.findViewById(R.id.messageSystemInChat) as TextView
                return
            }
            imageMessage = view.findViewById(R.id.imageMessage) as ImageView
            contentMessage = view.findViewById(R.id.contentMessage) as TextView
            timeMessage = view.findViewById(R.id.timeMessage) as TextView
            if(viewType == CONSTANT.VIEW_TYPE_RECEIVED_MESSAGE){
                Firebase.firestore.collection(CONSTANT.KEY_USER).document(message.senderId)
                    .get().addOnSuccessListener { value ->
                        //val image = value.getString(CONSTANT.KEY_USER_IMAGE)
                        //imageMessage.setImageBitmap(image?.let { getImage(it) })
                    }.addOnFailureListener { e ->
                        Log.d("Chat Fragment", "" + e.printStackTrace())
                    }
            }
            else {
                  //imageMessage.setImageBitmap(getImage(user.image))
            }
            contentMessage.text = message.message
            timeMessage.text = message.timeMessage.toString()
        }
    }
    private class NoteDiffCallBack : DiffUtil.ItemCallback<ChatMessage>(){
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.senderId == newItem.senderId && oldItem.timeMessage.equals(newItem.timeMessage)
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }

    }
    private inner class ChatRecyclerAdapter(var chatMessages : MutableList<ChatMessage> ) :
        ListAdapter<ChatMessage, ChatHolder>(
            AsyncDifferConfig.Builder<ChatMessage>(NoteDiffCallBack())
                .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
                .build()
        )
    {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
            if(viewType == CONSTANT.VIEW_TYPE_MESSAGE_SYSTEM){
                return ChatHolder(layoutInflater.inflate(R.layout.message_system, parent, false))
            }
            if(viewType == CONSTANT.VIEW_TYPE_RECEIVED_MESSAGE){
                return ChatHolder(layoutInflater.inflate(R.layout.received_message, parent, false))
            }
            return ChatHolder(layoutInflater.inflate(R.layout.sent_message, parent, false))
        }

        override fun onBindViewHolder(holder: ChatHolder, position: Int) {
            holder.bind(chatMessages[position], getItemViewType(position))
        }

        override fun getItemViewType(position: Int): Int {
            if(chatMessages[position].senderId == "1111"){
                return CONSTANT.VIEW_TYPE_MESSAGE_SYSTEM
            }
            if(user.userId.equals(chatMessages[position].senderId)){
                return CONSTANT.VIEW_TYPE_SEND_MESSAGE
            }
            else {
                return CONSTANT.VIEW_TYPE_RECEIVED_MESSAGE
            }
        }

    }
    companion object {
        fun newInstance(user: User, group: Group) : ChatFragment{
            var args = Bundle().apply {
                putSerializable(CONSTANT.KEY_GROUP, group)
                putSerializable(CONSTANT.KEY_USER, user)
            }
            return ChatFragment().apply {
                arguments = args
            }
        }
    }
    private fun getImage(encodeImage : String) : Bitmap {
        val bytes = Base64.decode(encodeImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    private fun addNewMessage(message : String ){
        var messageMap = hashMapOf(
            CONSTANT.KEY_MESSAGE_SENDER_ID to user.userId,
            CONSTANT.KEY_MESSAGE_CONTENT to message,
            CONSTANT.KEY_MESSAGE_TIME_SEND to Date()
        )
        Firebase.firestore.collection(CONSTANT.KEY_GROUP).document(group.groupId).collection(CONSTANT.KEY_MESSAGE).add(messageMap)
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
                        /*val hashNewMessage = mapOf(
                            CONSTANT.KEY_MESSAGE to mapOf(
                                CONSTANT.KEY_MESSAGE_CONTENT to "${user.name} is ${userAdded.name} into this group",
                                CONSTANT.KEY_MESSAGE_TIME_SEND to Date(),
                                CONSTANT.KEY_MESSAGE_SENDER_ID to "1111"
                            )
                        )*/
                        db.collection(CONSTANT.KEY_GROUP).document(group.groupId)
                            .update(CONSTANT.KEY_GROUP_LIST_MEMBER, FieldValue.arrayUnion(userAdded.userId))
                        //db.collection(CONSTANT.KEY_GROUP).document(group.groupId)
                            //.collection(CONSTANT.KEY_MESSAGE).add(hashNewMessage)
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