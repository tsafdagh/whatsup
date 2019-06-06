package com.mirai.whatsup.fragment


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import com.mirai.whatsup.R
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


class MyAccountFragment : Fragment() {

    private val RC_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray
    private var pictureJustChanged = false
    private lateinit var selectedImagePath : Uri
    private lateinit var sessionUser: FirebaseUser;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_my_account, container, false)
        sessionUser = FirebaseAuth.getInstance().currentUser!!
        view.apply {
            imageView_profile_picture.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }

                startActivityForResult(Intent.createChooser(intent, "Selectionnez une image"), RC_SELECT_IMAGE)
            }
            btn_save.setOnClickListener {
                if (::selectedImageBytes.isInitialized)
                    StorageUtil.uploadFromLocalFile(selectedImagePath) { imagePath: String ->

                        FireStoreUtil.updateCurrentUser(
                            editText_name.text.toString(),
                            editText_bio.text.toString(), imagePath)
                        toast("Image Saved Successfully")
                    }
                else
                    StorageUtil.uploadFromLocalFile(selectedImagePath) { imagePath ->
                        FireStoreUtil.updateCurrentUser(
                            editText_name.text.toString(),
                            editText_bio.text.toString(), null
                        )
            }
                toast("saving")

            }

            btn_sign_out.setOnClickListener {
                /*AuthUI.getInstance()
                    .signOut(this@MyAccountFragment.context!!)
                    .addOnCompleteListener {
                        startActivity(intentFor<SingleSignInActivity>().newTask().clearTask())
                    }*/
                FirebaseAuth.getInstance().signOut()
                startActivity(intentFor<SplashActivity>().newTask().clearTask())

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

            toast("name "+user.name)
            toast("bio "+user.bio)
            toast("url "+user.profilePicturePath)


            if (this@MyAccountFragment.isVisible) {
                editText_name.setText(user.name)
                editText_bio.setText(user.bio)

                if (!pictureJustChanged && user.profilePicturePath != null){
                    imageView_profile_picture.clearColorFilter()
                    GlideApp.with(this)
                        .load(user.profilePicturePath)
                        .placeholder(R.drawable.ic_account_box_black_24dp)
                        .into(imageView_profile_picture)
                }
            }
        }
    }
}
