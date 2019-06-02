package com.mirai.whatsup

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.ui.idp.SingleSignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.mirai.whatsup.authentifiaction.SigninActivity
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null)
            startActivity<SigninActivity>()
        else
            startActivity<MainActivity>()
        finish()
    }
}