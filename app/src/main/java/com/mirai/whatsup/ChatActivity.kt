package com.mirai.whatsup

import android.app.AlertDialog
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.mirai.whatsup.entities.MessageType
import com.mirai.whatsup.entities.TextMessage
import com.mirai.whatsup.receycleView.item.TextMessageItem
import com.mirai.whatsup.utils.FireStoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.toast
import java.util.*
import android.media.RingtoneManager
import android.media.Ringtone
import com.mirai.whatsup.utils.FirebaseMlKitUtil
import org.jetbrains.anko.indeterminateProgressDialog


class ChatActivity : AppCompatActivity() {

    private lateinit var messageListenerRegistration: ListenerRegistration
    private var shouldInitRecycleView = true
    private lateinit var messageSection: Section


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserid = intent.getStringExtra(AppConstants.USER_ID)
        FireStoreUtil.getorcreateChatChannel(otherUserid, onComplete = { channelId ->
            messageListenerRegistration =
                FireStoreUtil.addChatMessagesListeber(channelId, this, this::updateRecycleView)

            imageView_send.setOnClickListener {
                val messageText = TextMessage(
                    editText_message.text.toString(),
                    Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser!!.uid, MessageType.TEXT
                )
                editText_message.setText("")
                FireStoreUtil.sendMessage(messageText, channelId)
            }

            fab_send_image.setOnClickListener {
                //TODO: send image message
            }

        })
    }

    private fun updateRecycleView(messages: List<Item>) {
        fun init(){
            recycler_view_messages.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupAdapter<ViewHolder>().apply {
                    messageSection = Section(messages)
                    this.add(messageSection)
                    setOnItemClickListener(onItemClick)
                }
            }

            shouldInitRecycleView = false
        }

        fun updateItem()= messageSection.update(messages)

        if(shouldInitRecycleView)
            init()
        else{
            updateItem()
            // on joue le sons de la notification
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        }



        recycler_view_messages.scrollToPosition((recycler_view_messages.adapter?.itemCount ?: 1) -1)
    }

    private val onItemClick = OnItemClickListener{item, view ->
        if(item is TextMessageItem){
            val progressdialog = indeterminateProgressDialog("Traduction en cours...")
            FirebaseMlKitUtil.translateToEnglish(item.message.text, onComplete = {
                if(it.equals("-1")) {
                    toast("Traduction échouée")
                    progressdialog.dismiss()
                }else{
                    progressdialog.dismiss()
                    showTranslatedmessage(it)
                }
            })
        }
    }

    fun showTranslatedmessage(text: String){

        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Tanslated message")
            setMessage(text)
            setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                // Do something when user press the positive button
            })
            show()

        }


    }
}
