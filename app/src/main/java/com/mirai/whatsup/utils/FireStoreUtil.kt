package com.mirai.whatsup.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mirai.whatsup.entities.*
import com.mirai.whatsup.receycleView.item.ImageMessageItem
import com.mirai.whatsup.receycleView.item.PersonItem
import com.mirai.whatsup.receycleView.item.TextMessageItem
import com.xwray.groupie.kotlinandroidextensions.Item

object FireStoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document(
            "users/${FirebaseAuth.getInstance().currentUser?.uid ?: throw NullPointerException("UID is null..")}"
        )

    private val chatChannelCollectionRef = firestoreInstance.collection("chatchannels")

    fun initCurrentUserIfFirstTime(fileUrl: String, onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(
                    FirebaseAuth.getInstance().currentUser?.email ?: FirebaseAuth.getInstance().currentUser?.displayName
                    ?: "",
                    FirebaseAuth.getInstance().currentUser?.phoneNumber ?: "",
                    fileUrl ?: ""
                )
                currentUserDocRef.set(newUser).addOnSuccessListener { onComplete() }
            } else
                onComplete()
        }
    }

    fun updateCurrentUser(name: String = "", bio: String = "", profilPicturePath: String? = null) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (bio.isNotBlank()) userFieldMap["bio"] = bio
        if (profilPicturePath != null)
            userFieldMap["profilePicturePath"] = profilPicturePath
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java)!!)
            }
    }


    // cette méthode permet de recupérer la liste des utilisateurs
    fun addUserListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("users")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "Users listener error?", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach {
                    if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                        items.add(PersonItem(it.toObject(User::class.java)!!, it.id, context))
                }

                onListen(items)
            }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()


    /*
    cette méthode a pour but de creer ou de recupérer la liste de chaines de chat existant
     */
    fun getorcreateChatChannel(
        otheruserId: String,
        onComplete: (channelid: String) -> Unit
    ) {
        currentUserDocRef.collection("engagedChatChannels")
            .document(otheruserId).get().addOnSuccessListener {
                if (it.exists()) {
                    onComplete(it["channelId"] as String)
                    return@addOnSuccessListener
                }

                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
                val newChannel = chatChannelCollectionRef.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserId, otheruserId)))

                currentUserDocRef.collection("engagedChatChannels")
                    .document(otheruserId)
                    .set(mapOf("channelId" to newChannel.id))

                firestoreInstance.collection("users").document(otheruserId)
                    .collection("engagedChatChannels")
                    .document(currentUserId)
                    .set(mapOf("channelId" to newChannel.id))

                onComplete(newChannel.id)
            }
    }


    /*
    Cette méthode retourne la liste des chat dans une chaine donnée
     */
    fun addChatMessagesListeber(
        channelId: String, context: Context,
        onListner: (List<Item>) -> Unit
    ): ListenerRegistration {
        return chatChannelCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "ChatMessageslistener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach {
                    if (it["type"] == MessageType.TEXT)
                        items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!, context))
                    else
                        items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!, context))
                    return@forEach
                }

                onListner(items)

            }
    }

    fun sendMessage(message: Message, channelId: String) {
        chatChannelCollectionRef.document(channelId)
            .collection("messages")
            .add(message)
    }


}