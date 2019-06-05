package com.mirai.whatsup.example

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mirai.whatsup.R
import com.mirai.whatsup.entities.User
import kotlinx.android.synthetic.main.activity_adding_document_and_collection.*
import org.jetbrains.anko.toast

class managerUserProfile : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding_document_and_collection)

        btn_add_user.setOnClickListener {
            getCurrentUser { user: User ->
                toast("getting user ${user.name}")
            }
        }

    }

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private lateinit var currentUserDocRef: DocumentReference

    private fun initCurentUser(onComplete: () -> Unit) {

        currentUserDocRef = getDocumentReference()
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(FirebaseAuth.getInstance().currentUser?.displayName ?: "", "", null)
                currentUserDocRef.set(newUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "user saved succefully", Toast.LENGTH_LONG).show()
                        onComplete()
                    }
            } else {
                Toast.makeText(this, "user alredy exist", Toast.LENGTH_LONG).show()
                onComplete()
            }


        }
    }

    fun updateCurrentUser(name: String = "", bio: String = "", profilPicturePath: String? = null) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (bio.isNotBlank()) userFieldMap["bio"] = bio
        if (profilPicturePath != null)
            userFieldMap["profilePicturePath"] = profilPicturePath

        toast("enregistrer")

    }


     fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef = getDocumentReference()
        currentUserDocRef.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java)!!)
            }
    }

    private fun getDocumentReference(): DocumentReference {
        return firestoreInstance.document(
            "users/${FirebaseAuth.getInstance().uid ?: throw NullPointerException("UID is null..")}"
        )
    }
}
