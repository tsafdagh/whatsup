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
import kotlinx.android.synthetic.main.activity_signin.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN){
            val response = IdpResponse.fromResultIntent(data)

            if(resultCode == Activity.RESULT_OK){
                val progressdialog = indeterminateProgressDialog ( "setting up your account" )

                startActivity(intentFor<MainActivity>().newTask().clearTask())
                progressdialog.dismiss()
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                if(response == null) return

                when(response.error?.errorCode){
                    ErrorCodes.NO_NETWORK ->
                        longSnackbar(constraint_layout, "Pa de réseau")
                    ErrorCodes.UNKNOWN_ERROR ->
                        longSnackbar(constraint_layout, "Erreur inconue")
                }
            }
        }
    }
}
