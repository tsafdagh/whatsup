package com.mirai.whatsup

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import com.google.firebase.firestore.ListenerRegistration
import com.mirai.whatsup.utils.FireStoreUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import org.jetbrains.anko.toast

class ChatActivity : AppCompatActivity() {

    private lateinit var messageListenerRegistration: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserid = intent.getStringExtra(AppConstants.USER_ID)
        FireStoreUtil.getorcreateChatChannel(otherUserid, onComplete = {channelId->
            messageListenerRegistration = FireStoreUtil.addChatMessagesListeber(channelId, this, this::onMessagesChanged)
        })
    }

    private fun onMessagesChanged(message: List<Item>){
        toast("onMessagesChangerRunning")
    }
}
