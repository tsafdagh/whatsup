package com.mirai.whatsup.authentifiaction

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.mirai.whatsup.MainActivity
import com.mirai.whatsup.R
import com.mirai.whatsup.utils.StorageUtil
import com.mirai.whatsup.utils.FireStoreUtil
import kotlinx.android.synthetic.main.activity_signin.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.longSnackbar

class SigninActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 1
    private val signInProviders = listOf(
        AuthUI.IdpConfig.EmailBuilder()
            .setAllowNewAccounts(true)
            .setRequireName(true)
            .build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        account_sign_in.setOnClickListener {
            val intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(signInProviders)
                .setLogo(R.drawable.emoji_icon)
                .build()
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    private val RC_SELECT_IMAGE = 5
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val progressdialog = indeterminateProgressDialog("setting up your account")
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }
                startActivityForResult(Intent.createChooser(intent, "Image de profil"), RC_SELECT_IMAGE)
                progressdialog.dismiss()


            }else if (resultCode == Activity.RESULT_CANCELED) {
                if (response == null) return

                when (response.error?.errorCode) {
                    ErrorCodes.NO_NETWORK ->
                        longSnackbar(constraint_layout, getString(R.string.erreur_pas_reseau))
                    ErrorCodes.UNKNOWN_ERROR ->
                        longSnackbar(constraint_layout, getString(R.string.unknow_error))
                }
            }
        }else if(requestCode == RC_SELECT_IMAGE){
            toast("Image Path ok")

            var selectedImagePath = data?.data

            selectedImagePath?.let {
                StorageUtil.uploadFromLocalFile(it, onSuccess = {
                    toast("URL: ${it}" )
                    FireStoreUtil.initCurrentUserIfFirstTime(it, onComplete = {
                        startActivity(intentFor<MainActivity>().newTask().clearTask())
                        toast("User saved succesfully" )

                    })
                })
            }

        }
    }
}
