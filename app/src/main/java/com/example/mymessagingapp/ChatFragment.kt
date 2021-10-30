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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.example.mymessagingapp.data.ChatMessage
import com.example.mymessagingapp.data.Group
import com.example.mymessagingapp.data.User
import com.example.messapp.R
import com.example.mymessagingapp.modelview.ChatListViewModelFactory
import com.example.mymessagingapp.modelview.ChatViewModelFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executors

class ChatFragment : Fragment() {
    private lateinit var user : User
    private lateinit var group : Group
    private lateinit var nameReceiver : TextView
    private lateinit var imageReceiver : ImageView
    private lateinit var settingButton : ImageButton
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
        settingButton = view.findViewById(R.id.settingChatMessage) as ImageButton
        messageRecyclerView = view.findViewById(R.id.chatListRecyclerView) as RecyclerView
        var linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        messageRecyclerView.layoutManager = linearLayoutManager
        messageRecyclerView.adapter = adapter
        sendingMessage = view.findViewById(R.id.sendMessage) as EditText
        sendingMessageButton = view.findViewById(R.id.sendMessageButton) as Button
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
    private inner class ChatHolder(view : View) : RecyclerView.ViewHolder(view) {
        private lateinit var message : ChatMessage
        private val imageMessage = view.findViewById(R.id.imageMessage) as ImageView
        private var contentMessage = view.findViewById(R.id.contentMessage) as TextView
        private var timeMessage = view.findViewById(R.id.timeMessage) as TextView
        fun bind(chat: ChatMessage, viewType : Int){
            this.message = message
            if(viewType == CONSTANT.VIEW_TYPE_RECEIVED_MESSAGE){
                Firebase.firestore.collection(CONSTANT.KEY_USER).document(message.senderId)
                    .get().addOnSuccessListener { value ->
                        imageMessage.setImageBitmap(getImage(value.toObject<User>()!!.image))
                    }.addOnFailureListener { e ->
                        Log.d("Chat Fragment", "" + e.printStackTrace())
                    }
            }
            else {
                  imageMessage.setImageBitmap(getImage(user.image))
            }
        }
    }
    private class NoteDiffCallBack : DiffUtil.ItemCallback<ChatMessage>(){
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.groupId == newItem.groupId && oldItem.senderId == newItem.senderId && oldItem.timeMessage.equals(newItem.timeMessage)
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
            if(viewType == CONSTANT.VIEW_TYPE_RECEIVED_MESSAGE){
                return ChatHolder(layoutInflater.inflate(R.layout.received_message, parent, false))
            }
            return ChatHolder(layoutInflater.inflate(R.layout.sent_message, parent, false))
        }

        override fun onBindViewHolder(holder: ChatHolder, position: Int) {
            holder.bind(chatMessages[position], getItemViewType(position))
        }

        override fun getItemViewType(position: Int): Int {
            if(user.userId == chatMessages[position].senderId){
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
}