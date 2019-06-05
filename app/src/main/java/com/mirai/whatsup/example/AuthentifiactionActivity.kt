package com.mirai.whatsup.example

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mirai.whatsup.R
import kotlinx.android.synthetic.main.activity_authentifiaction.*

class AuthentifiactionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentifiaction)

        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val curentUser = auth.currentUser
        updateUI(curentUser)
    }

    private fun updateUI(curentUser: FirebaseUser?) {
        editText_email_auth.setText(curentUser?.email.toString())
        editText_full_name.setText(curentUser?.displayName.toString())

        Toast.makeText(applicationContext, "image url: ${curentUser?.let { curentUser.photoUrl}}", Toast.LENGTH_LONG).show()
        //curentUser.photoUrl
    }


}
