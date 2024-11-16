package com.example.easychat.utils

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat

object FirebaseUtil {
    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().uid
    }

    val isLoggedIn: Boolean
        get() {
            if (currentUserId() != null) {
                return true
            }
            return false
        }

    fun currentUserDetails(): DocumentReference {
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId()!!)
    }

    fun allUserCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("users")
    }

    fun getChatroomReference(chatroomId: String): DocumentReference {
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId)
    }

    fun getChatroomMessageReference(chatroomId: String): CollectionReference {
        return getChatroomReference(chatroomId).collection("chats")
    }

    fun getChatroomId(userId1: String, userId2: String): String {
        return if (userId1.hashCode() < userId2.hashCode()) {
            userId1 + "_" + userId2
        } else {
            userId2 + "_" + userId1
        }
    }

    fun allChatroomCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("chatrooms")
    }

    fun getOtherUserFromChatroom(userIds: List<String?>): DocumentReference {
        return if (userIds[0] == currentUserId()) {
            allUserCollectionReference().document(userIds[1]!!)
        } else {
            allUserCollectionReference().document(userIds[0]!!)
        }
    }

    fun timestampToString(timestamp: Timestamp): String {
        return SimpleDateFormat("HH:MM").format(timestamp.toDate())
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    val currentProfilePicStorageRef: StorageReference
        get() = FirebaseStorage.getInstance().reference.child("profile_pic")
            .child(currentUserId()!!)

    fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference {
        return FirebaseStorage.getInstance().reference.child("profile_pic")
            .child(otherUserId)
    }
}










