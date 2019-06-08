package com.mirai.whatsup.fragment


import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import com.mirai.whatsup.SplashActivity
import com.mirai.whatsup.glide.GlideApp
import com.mirai.whatsup.utils.FireStoreUtil
import com.mirai.whatsup.utils.StorageUtil
import kotlinx.android.synthetic.main.fragment_my_account.*
import kotlinx.android.synthetic.main.fragment_my_account.view.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast
import java.io.ByteArrayOutputStream
import com.google.android.gms.tasks.OnSuccessListener
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.support.v4.indeterminateProgressDialog


class MyAccountFragment : Fragment() {

    private val RC_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray
    private var pictureJustChanged = false
    private lateinit var selectedImagePath: Uri
    private lateinit var sessionUser: FirebaseUser
    private var isImageSelected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(com.mirai.whatsup.R.layout.fragment_my_account, container, false)
        sessionUser = FirebaseAuth.getInstance().currentUser!!
        isImageSelected =false
        view.apply {
            imageView_profile_picture.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                isImageSelected =true
                startActivityForResult(Intent.createChooser(intent, "Selectionnez une image"), RC_SELECT_IMAGE)
            }
            btn_save.setOnClickListener {
                val progressdialog = indeterminateProgressDialog("Mise à jours en cours")
                if (isImageSelected)
                    StorageUtil.uploadFromLocalFile(selectedImagePath) { imagePath: String ->
                        FireStoreUtil.updateCurrentUser(
                            editText_name.text.toString(),
                            editText_bio.text.toString(), imagePath
                        )
                        progressdialog.dismiss()
                    }
                else
                    FireStoreUtil.updateCurrentUser(
                        editText_name.text.toString(),
                        editText_bio.text.toString(), null
                    )
                progressdialog.dismiss()
                toast("Mise à jour du profils éffectuer")
            }

            btn_sign_out.setOnClickListener {
                AuthUI.getInstance()
                    .signOut(this@MyAccountFragment.context!!)
                    .addOnSuccessListener {
                        startActivity(intentFor<SplashActivity>().newTask().clearTask())
                    }
                //FirebaseAuth.getInstance().signOut()
            }

            btn_sign_dele_caount.setOnClickListener {

                val dialogBuilder = AlertDialog.Builder(this.context).apply {
                    setMessage("Voulez vous vraiment supprimer votre compte??")
                        // if the dialog is cancelable
                        .setCancelable(false)
                        // positive button text and action
                        .setPositiveButton("OUI", DialogInterface.OnClickListener { dialog, id ->
                            AuthUI.getInstance().delete(this@MyAccountFragment.context!!)
                                .addOnSuccessListener {
                                    //TODO suppression de l'utiolisateur dans la base de données firebase
                                    startActivity(intentFor<SplashActivity>().newTask().clearTask())
                                }
                        })
                        // negative button text and action
                        .setNegativeButton("NON", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                        })
                }
                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("Confirmation de suppression")
                // show alert dialog
                alert.show()
            }
        }

        return view

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImagePath)

            val outPutStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outPutStream)
            selectedImageBytes = outPutStream.toByteArray()

            GlideApp.with(this)
                .load(selectedImageBytes)
                .into(imageView_profile_picture)

            pictureJustChanged = true

        }
    }

    override fun onStart() {
        super.onStart()
        FireStoreUtil.getCurrentUser { user ->

            if (this@MyAccountFragment.isVisible) {
                editText_name.setText(user.name)
                editText_bio.setText(user.bio)

                if (!pictureJustChanged && user.profilePicturePath != null) {
                    imageView_profile_picture.clearColorFilter()
                    GlideApp.with(this)
                        .load(user.profilePicturePath)
                        .placeholder(com.mirai.whatsup.R.drawable.ic_account_box_black_24dp)
                        .into(imageView_profile_picture)
                }
            }
        }
    }
}
