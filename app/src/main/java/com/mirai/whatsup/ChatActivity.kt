package com.mirai.whatsup

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
import android.provider.CalendarContract
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import com.mirai.whatsup.entities.ImageMessage
import com.mirai.whatsup.option.Configuration
import com.mirai.whatsup.utils.FirebaseMlKitUtil
import com.mirai.whatsup.utils.StorageUtil
import org.jetbrains.anko.indeterminateProgressDialog
import java.io.ByteArrayOutputStream


private const val RC_SELECT_IMAGE = 2
class ChatActivity : AppCompatActivity() {

    private lateinit var currenChannelId: String

    private lateinit var messageListenerRegistration: ListenerRegistration
    private var shouldInitRecycleView = true
    private lateinit var messageSection: Section
    private lateinit var otheruserName: String
    private lateinit var otherUserUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)
        otheruserName = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserid = intent.getStringExtra(AppConstants.USER_ID)
        otherUserUid = intent.getStringExtra(AppConstants.USER_ID)
        FireStoreUtil.getorcreateChatChannel(otherUserid, onComplete = { channelId ->
            currenChannelId = channelId
            messageListenerRegistration =
                FireStoreUtil.addChatMessagesListeber(channelId, this, this::updateRecycleView)

            imageView_send.setOnClickListener {
                var textMessage = editText_message.text.toString()
                if (Configuration.istranslateMessaToEnglishActived) {
                    val progressdialog = indeterminateProgressDialog("Traduction en cours...")
                    FirebaseMlKitUtil.translateToEnglish(textMessage, onComplete = { stransletedMessage: String ->
                        if (stransletedMessage == "-1") {
                            toast("Traduction échouée")
                            progressdialog.dismiss()
                        } else {
                            progressdialog.dismiss()
                            val messageText = TextMessage(
                                stransletedMessage,
                                Calendar.getInstance().time,
                                FirebaseAuth.getInstance().currentUser!!.uid, MessageType.TEXT
                            )
                            editText_message.setText("")
                            FireStoreUtil.sendMessage(messageText, channelId)
                        }
                    })

                } else {
                    val messageText = TextMessage(
                        textMessage,
                        Calendar.getInstance().time,
                        FirebaseAuth.getInstance().currentUser!!.uid, MessageType.TEXT
                    )
                    editText_message.setText("")
                    FireStoreUtil.sendMessage(messageText, channelId)
                }
            }

            fab_send_image.setOnClickListener {

                val intent = Intent().apply {
                    type ="image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                }

                startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), RC_SELECT_IMAGE)
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
                data != null && data.data != null){
            val selectedimagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(contentResolver, selectedimagePath)
            val outputStream = ByteArrayOutputStream()

            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90,outputStream)
            val selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadMessageImageFromByteArray(selectedImageBytes,onSuccess = {imagepath:String->
                val messageToSend = ImageMessage(imagepath, Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser!!.uid)

                FireStoreUtil.sendMessage(messageToSend, currenChannelId)
            })
        }
    }

    private fun updateRecycleView(messages: List<Item>) {
        fun init() {
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

        fun updateItem() = messageSection.update(messages)

        if (shouldInitRecycleView)
            init()
        else {
            updateItem()
            // on joue le sons de la notification
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        }

        recycler_view_messages.scrollToPosition((recycler_view_messages.adapter?.itemCount ?: 1) - 1)
    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is TextMessageItem) {
            val progressdialog = indeterminateProgressDialog("Traduction en cours...")
            FirebaseMlKitUtil.translateToEnglish(item.message.text, onComplete = {
                if (it.equals("-1")) {
                    toast("Traduction échouée")
                    progressdialog.dismiss()
                } else {
                    progressdialog.dismiss()
                    showTranslatedmessage(it)
                }
            })
        }
    }

    fun showTranslatedmessage(text: String) {

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
