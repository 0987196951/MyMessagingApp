package com.example.mymessagingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import com.example.mymessagingapp.interfaces.CallBackWhenSeeMoreInfoGroup
import com.example.mymessagingapp.modelview.ChatViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private val TAG = "ChatFragmentListener"
class ChatFragment : Fragment(){
    private lateinit var user : User
    private lateinit var group : Group
    private var groupMapping : String?= null
    private lateinit var nameReceiver : TextView
    private lateinit var imageReceiver : ImageView
    private lateinit var moreInfo : ImageButton
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
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chat_message, container, false)
        nameReceiver = view.findViewById(R.id.nameReceiverChatMessage) as TextView
        imageReceiver = view.findViewById(R.id.imageReceiverChatMessage) as ImageView
        moreInfo = view.findViewById(R.id.moreInfoGroup) as ImageButton
        if(!group.isGroup) {
            moreInfo.visibility = View.GONE
        }
        messageRecyclerView = view.findViewById(R.id.chatListRecyclerView) as RecyclerView
        var linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.isSmoothScrollbarEnabled = true
        messageRecyclerView.layoutManager = linearLayoutManager
        sendingMessage = view.findViewById(R.id.sendMessage) as EditText
        sendingMessageButton = view.findViewById(R.id.sendMessageButton) as Button
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameReceiver.text = group.nameGroup
        imageReceiver.setImageBitmap(getImage(group.imageGroup))
        adapter = ChatRecyclerAdapter()
        messageRecyclerView.adapter = adapter
        chatViewModel.listMessage.observe(
            viewLifecycleOwner,
            Observer { messes ->
                messes?.let {
                    Log.d("ChatFragment", "size of listMessage is ${messes.size}")
                    adapter.submitList(messes)
                    messageRecyclerView.scrollToPosition(adapter.itemCount-1)
                    //messageRecyclerView.adapter = adapter
                }
            })

    }

    override fun onStart() {
        super.onStart()
        sendingMessageButton.setOnClickListener { v ->
            val s = sendingMessage.text.toString().trim()
            sendingMessage.text.clear()
            addNewMessage(s)
        }
        moreInfo.setOnClickListener { v ->
            (requireContext() as CallBackWhenSeeMoreInfoGroup).seeForInfoGroup(group)
        }
    }

    private class NoteDiffCallBack : DiffUtil.ItemCallback<ChatMessage>(){
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            //Log.d(TAG, "old : $oldItem new $newItem")
            return oldItem.senderId == newItem.senderId && oldItem.timeMessage == newItem.timeMessage
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            //Log.d(TAG, "old : $oldItem new $newItem")
            return oldItem == newItem
        }

    }
    private inner class ChatRecyclerAdapter :
        ListAdapter<ChatMessage, ChatRecyclerAdapter.ChatHolder>(
            AsyncDifferConfig.Builder(NoteDiffCallBack())
                .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
                .build()
        )
    {
        private var mapImage : MutableMap<String, String> = mutableMapOf()
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
            val chat = getItem(position)
            holder.bind(chat, getItemViewType(position))
        }

        override fun getItemViewType(position: Int): Int {
            if(getItem(position).senderId == CONSTANT.KEY_MESSAGE_SYSTEM_ID){
                return CONSTANT.VIEW_TYPE_MESSAGE_SYSTEM
            }
            return if(user.userId == getItem(position).senderId){
                CONSTANT.VIEW_TYPE_SEND_MESSAGE
            } else {
                CONSTANT.VIEW_TYPE_RECEIVED_MESSAGE
            }
        }

        override fun submitList(list: MutableList<ChatMessage>?) {
            super.submitList(list?.let { it.toList() })
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
                    contentMessage.text = chat.message
                    return
                }
                imageMessage = view.findViewById(R.id.imageMessage) as ImageView
                contentMessage = view.findViewById(R.id.contentMessage) as TextView
                timeMessage = view.findViewById(R.id.timeMessage) as TextView
                if(viewType == CONSTANT.VIEW_TYPE_RECEIVED_MESSAGE){
                    val image = mapImage.get(chat.senderId)
                    if(image == null){
                        Firebase.firestore.collection(CONSTANT.KEY_USER).document(chat.senderId)
                            .get().addOnSuccessListener { value ->
                                if(value != null){
                                    Log.d(TAG, "request chatFragment")
                                    val imageUser = value.data?.get(CONSTANT.KEY_USER_IMAGE) as String
                                    imageMessage.setImageBitmap(getImage(imageUser))
                                    mapImage.put(chat.senderId, imageUser)
                                }
                            }
                    }
                    else{
                        imageMessage.setImageBitmap(getImage(image))
                    }
                }
                else {
                    imageMessage.setImageBitmap(getImage(user.image))
                }
                contentMessage.text = message.message
                timeMessage.text = message.timeMessage.toString()
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
        if(message.isBlank()) return
        var messageMap = hashMapOf(
            CONSTANT.KEY_MESSAGE_SENDER_NAME to user.name,
            CONSTANT.KEY_MESSAGE_SENDER_ID to user.userId,
            CONSTANT.KEY_MESSAGE_CONTENT to message,
            CONSTANT.KEY_MESSAGE_TIME_SEND to Date()
        )
        Firebase.firestore.collection(CONSTANT.KEY_GROUP).document(group.groupId).collection(CONSTANT.KEY_MESSAGE).add(messageMap)
        if(!group.isGroup){
            if(groupMapping == null){
                Firebase.firestore.collection(CONSTANT.KEY_GROUP).document(group.groupId).get()
                    .addOnSuccessListener { value ->
                        if(value != null){
                            groupMapping = value.data?.get(CONSTANT.KEY_GROUP_MAPPING) as String
                            Firebase.firestore.collection(CONSTANT.KEY_GROUP).document(groupMapping!!)
                                .collection(CONSTANT.KEY_MESSAGE).add(messageMap)
                        }
                    }
            }
            else {
                Firebase.firestore.collection(CONSTANT.KEY_GROUP).document(groupMapping!!)
                    .collection(CONSTANT.KEY_MESSAGE).add(messageMap)
            }
        }
    }
}