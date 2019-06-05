package com.mirai.whatsup.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mirai.whatsup.entities.User

object FireStoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document(
            "users/${FirebaseAuth.getInstance().uid ?: throw NullPointerException("UID is null..")}"
        )

    fun initCurrentUserIfFirstTime(fileUrl: String, onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(
                    FirebaseAuth.getInstance().currentUser?.email ?: "",
                    FirebaseAuth.getInstance().currentUser?.displayName ?: "",
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
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java)!!)
            }
    }
}