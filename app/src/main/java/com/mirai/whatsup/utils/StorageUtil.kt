package com.mirai.whatsup.utils

import android.net.Uri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.*

object StorageUtil {

    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val currentUserRef: StorageReference
        get() = storageInstance.reference
            .child(FirebaseAuth.getInstance().currentUser?.uid ?: throw NullPointerException("IUD is null."))

    fun uploadProfilePhoto(
        imageByte: ByteArray,
        onSuccess: (imagePath: String) -> Unit
    ) {
        val ref = currentUserRef.child("profilePicture/${UUID.nameUUIDFromBytes(imageByte)}")
        ref.putBytes(imageByte)
            .addOnSuccessListener { onSuccess(ref.path) }
    }

    fun pathToReference(path: String) = storageInstance.getReference(path)


    // Create a storage reference from our app
    val storageRef = FirebaseStorage.getInstance().reference
    fun uploadFromLocalFile(filePath: Uri, onSuccess: (String) -> Unit) {
        var file = filePath
        val riversRef = storageRef.child(
            "users_profile/${FirebaseAuth.getInstance().currentUser!!.email
                ?: FirebaseAuth.getInstance().currentUser?.displayName}"
        )
        var uploadTask = file?.let { riversRef.putFile(it) }

// Register observers to listen for when the download is done or if it fails
        uploadTask!!.addOnFailureListener {

        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation riversRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    onSuccess(downloadUri.toString())
                } else {
                    // Handle failures
                    onSuccess("unploading error")

                }
            }
        }
    }

    fun uploadMessageImageFromByteArray(imagebytes: ByteArray, onSuccess: (imagePath: String) -> Unit) {

        val ref = currentUserRef.child("messages/${UUID.nameUUIDFromBytes(imagebytes)}")
        ref.putBytes(imagebytes)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }
    }


    fun uploadImageOfGroupe(idGroupe: String, filePath: Uri, onSuccess: (String) -> Unit) {
        var file = filePath
        val riversRef = storageRef.child(
            "chat_groupe_images/${idGroupe}"
        )
        var uploadTask = file?.let { riversRef.putFile(it) }

// Register observers to listen for when the download is done or if it fails
        uploadTask!!.addOnFailureListener {

        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation riversRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    onSuccess(downloadUri.toString())
                } else {
                    // Handle failures
                    onSuccess("unploading error")

                }
            }
        }
    }

}