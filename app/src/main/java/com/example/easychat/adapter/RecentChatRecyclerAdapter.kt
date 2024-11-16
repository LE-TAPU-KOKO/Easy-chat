package com.example.easychat.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.easychat.ChatActivity
import com.example.easychat.R
import com.example.easychat.adapter.RecentChatRecyclerAdapter.ChatroomModelViewHolder
import com.example.easychat.model.ChatroomModel
import com.example.easychat.model.UserModel
import com.example.easychat.utils.AndroidUtil
import com.example.easychat.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

class RecentChatRecyclerAdapter(
    options: FirestoreRecyclerOptions<ChatroomModel?>,
    context: Context?
) :
    FirestoreRecyclerAdapter<ChatroomModel, ChatroomModelViewHolder>(options) {
    var context: Context = context!!

    override fun onBindViewHolder(
        holder: ChatroomModelViewHolder,
        position: Int,
        model: ChatroomModel
    ) {
        FirebaseUtil.getOtherUserFromChatroom(model.userIds)
            .get().addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    val lastMessageSentByMe =
                        model.lastMessageSenderId == FirebaseUtil.currentUserId()


                    val otherUserModel = task.result.toObject(UserModel::class.java)
                    FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).downloadUrl
                        .addOnCompleteListener { t: Task<Uri?> ->
                            if (t.isSuccessful) {
                                val uri = t.result
                                AndroidUtil.setProfilePic(context, uri, holder.profilePic)
                            }
                        }


                    holder.usernameText.text = otherUserModel.getUsername()
                    if (lastMessageSentByMe) holder.lastMessageText.text =
                        "You : " + model.lastMessage
                    else holder.lastMessageText.text = model.lastMessage
                    holder.lastMessageTime.text =
                        FirebaseUtil.timestampToString(model.lastMessageTimestamp)

                    holder.itemView.setOnClickListener { v: View? ->
                        //navigate to chat activity
                        val intent = Intent(context, ChatActivity::class.java)
                        AndroidUtil.passUserModelAsIntent(intent, otherUserModel!!)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomModelViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false)
        return ChatroomModelViewHolder(view)
    }

    inner class ChatroomModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var usernameText: TextView = itemView.findViewById(R.id.user_name_text)
        var lastMessageText: TextView = itemView.findViewById(R.id.last_message_text)
        var lastMessageTime: TextView = itemView.findViewById(R.id.last_message_time_text)
        var profilePic: ImageView =
            itemView.findViewById(R.id.profile_pic_image_view)
    }
}
