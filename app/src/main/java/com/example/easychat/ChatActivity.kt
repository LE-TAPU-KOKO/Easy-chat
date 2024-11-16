package com.example.easychat

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.example.easychat.adapter.ChatRecyclerAdapter
import com.example.easychat.model.ChatMessageModel
import com.example.easychat.model.ChatroomModel
import com.example.easychat.model.UserModel
import com.example.easychat.utils.AndroidUtil
import com.example.easychat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder.build
import okhttp3.Request.Builder.header
import okhttp3.Request.Builder.post
import okhttp3.Request.Builder.url
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Arrays

class ChatActivity : AppCompatActivity() {
    var otherUser: UserModel? = null
    var chatroomId: String? = null
    var chatroomModel: ChatroomModel? = null
    var messageInput: EditText? = null
    var sendMessageBtn: ImageButton? = null
    var backBtn: ImageButton? = null
    var otherUsername: TextView? = null
    var recyclerView: RecyclerView? = null
    var imageView: ImageView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //get UserModel
        otherUser = AndroidUtil.getUserModelFromIntent(intent)
        chatroomId =
            FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId()!!, otherUser.getUserId())

        messageInput = findViewById(R.id.chat_message_input)
        sendMessageBtn = findViewById(R.id.message_send_btn)
        backBtn = findViewById(R.id.back_btn)
        otherUsername = findViewById(R.id.other_username)
        recyclerView = findViewById(R.id.chat_recycler_view)
        imageView = findViewById(R.id.profile_pic_image_view)
        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).downloadUrl
            .addOnCompleteListener { t: Task<Uri?> ->
                if (t.isSuccessful) {
                    val uri = t.result
                    AndroidUtil.setProfilePic(this, uri, imageView)
                }
            }



        backBtn.setOnClickListener(View.OnClickListener { v: View? ->
            onBackPressed()
        })
        otherUsername.setText(otherUser.getUsername())

        sendMessageBtn.setOnClickListener((View.OnClickListener { v: View? ->
            val message = messageInput.getText().toString().trim { it <= ' ' }
            if (message.isEmpty()) return@OnClickListener
            sendMessageToUser(message)
        }))

        orCreateChatroomModel
        setupChatRecyclerView()
    }

    fun setupChatRecyclerView() {
        val query = FirebaseUtil.getChatroomMessageReference(
            chatroomId!!
        )
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java).build()

        val adapter = ChatRecyclerAdapter(options, applicationContext)
        val manager = LinearLayoutManager(this)
        manager.reverseLayout = true
        recyclerView!!.layoutManager = manager
        recyclerView!!.adapter = adapter
        adapter.startListening()
        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerView!!.smoothScrollToPosition(0)
            }
        })
    }

    fun sendMessageToUser(message: String?) {
        chatroomModel.setLastMessageTimestamp(now.now())
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId())
        chatroomModel.setLastMessage(message)
        FirebaseUtil.getChatroomReference(chatroomId!!).set(chatroomModel!!)

        val chatMessageModel = ChatMessageModel(message, FirebaseUtil.currentUserId(), now.now())
        FirebaseUtil.getChatroomMessageReference(chatroomId!!).add(chatMessageModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    messageInput!!.setText("")
                    sendNotification(message)
                }
            }
    }

    val orCreateChatroomModel: Unit
        get() {
            FirebaseUtil.getChatroomReference(chatroomId!!).get()
                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                    if (task.isSuccessful) {
                        chatroomModel = task.result.toObject(ChatroomModel::class.java)
                        if (chatroomModel == null) {
                            //first time chat
                            chatroomModel = ChatroomModel(
                                chatroomId,
                                Arrays.asList<String?>(
                                    FirebaseUtil.currentUserId(),
                                    otherUser.getUserId()
                                ),
                                now.now(),
                                ""
                            )
                            FirebaseUtil.getChatroomReference(chatroomId!!).set(chatroomModel!!)
                        }
                    }
                }
        }

    fun sendNotification(message: String?) {
        FirebaseUtil.currentUserDetails().get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val currentUser = task.result.toObject(UserModel::class.java)
                    try {
                        val jsonObject = JSONObject()

                        val notificationObj = JSONObject()
                        notificationObj.put("title", currentUser.getUsername())
                        notificationObj.put("body", message)

                        val dataObj = JSONObject()
                        dataObj.put("userId", currentUser.getUserId())

                        jsonObject.put("notification", notificationObj)
                        jsonObject.put("data", dataObj)
                        jsonObject.put("to", otherUser.getFcmToken())

                        callApi(jsonObject)
                    } catch (e: Exception) {
                    }
                }
            }
    }

    fun callApi(jsonObject: JSONObject) {
        val JSON: MediaType = get.get("application/json; charset=utf-8")
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val body: RequestBody = create.create(jsonObject.toString(), JSON)
        val request: Request = Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer YOUR_API_KEY")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
            }
        })
    }
}