package com.mirai.whatsup

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.mirai.whatsup.entities.MessageType
import com.mirai.whatsup.entities.TextMessage
import com.mirai.whatsup.receycleView.item.TextMessageItem
import com.mirai.whatsup.utils.FireStoreUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.toast
import java.util.*
import android.media.RingtoneManager
import android.provider.CalendarContract
import android.provider.MediaStore
import android.support.v7.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.mirai.whatsup.entities.ImageMessage
import com.mirai.whatsup.option.Configuration
import com.mirai.whatsup.utils.FirebaseMlKitUtil
import com.mirai.whatsup.utils.StorageUtil
import com.xwray.groupie.*
import org.jetbrains.anko.Android
import org.jetbrains.anko.gray
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

        //on désactive la traduction automatique
        Configuration.istranslateMessaActived = false


        val otherUserid = intent.getStringExtra(AppConstants.USER_ID)
        otherUserUid = intent.getStringExtra(AppConstants.USER_ID)
        FireStoreUtil.getorcreateChatChannel(otherUserid, onComplete = { channelId ->
            currenChannelId = channelId
            messageListenerRegistration =
                FireStoreUtil.addChatMessagesListeber(channelId, this, this::updateRecycleView)

            imageView_send.setOnClickListener {
                var textMessage = editText_message.text.toString()
                if (Configuration.istranslateMessaActived) {
                    val progressdialog = indeterminateProgressDialog("Traduction en cours...")
                    FirebaseMlKitUtil.translateToAnyLanguage(textMessage, Configuration.oldLanguage, Configuration.translete_language, onComplete = { stransletedMessage: String ->
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
                    //setOnItemClickListener(onItemClick)
                    //setOnItemLongClickListener(onItemLongClick)
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

    private val onItemClick = OnItemClickListener{item, view ->


        if(item is TextMessageItem){
            val items = arrayOf("Traduire", "Marquer le message")
            val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustomStyle))
            view.setBackgroundColor(Color.TRANSPARENT)
            with(builder)
            {
                setTitle("Choisir une action")
                setItems(items) { dialog, which ->
                    when(items[which]){
                        "Traduire" ->{
                            showAnaylanguageTranslatedDialog(item.message.text)
                            if(!item.isSelectet){
                                view.setBackgroundColor(Color.RED)
                                item.isSelectet =true
                            }else{
                                item.isSelectet =false
                            }

                        }
                        "Marquer le message"->{
                            view.setBackgroundColor(Color.YELLOW)
                        }
                    }
                }
                /*  setPositiveButton("OK") { dialog:DialogInterface, which:Int ->
                      toast("selection OK")
                  }*/
                show()
            }
        }
         true
    }

    private fun showAnaylanguageTranslatedDialog(messsageSource:String){
        val items = arrayOf("Anglais","Français",  "Japonais")
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustomStyle))
        with(builder)
        {
            setTitle("Choisir La langue de traduction")
            setItems(items) { dialog, which ->
                when(items[which]){
                    "Anglais" ->{
                        toast("Anglais")
                        val progressdialog = indeterminateProgressDialog("Traduction en cours...")
                        FirebaseMlKitUtil.translateToAnyLanguage(messsageSource, FirebaseTranslateLanguage.FR, FirebaseTranslateLanguage.EN, onComplete = {
                            if (it.equals("-1")) {
                                toast("Traduction failled")
                                progressdialog.dismiss()
                            } else {
                                progressdialog.dismiss()
                                showTranslatedmessage(it)
                            }
                        })
                    }
                    "Français"->{
                        toast("Francais")
                        val progressdialog = indeterminateProgressDialog("In translate...")
                        FirebaseMlKitUtil.translateToAnyLanguage(messsageSource, FirebaseTranslateLanguage.EN, FirebaseTranslateLanguage.FR, onComplete = {
                            if (it.equals("-1")) {
                                toast("Traduction échouée")
                                progressdialog.dismiss()
                            } else {
                                progressdialog.dismiss()
                                showTranslatedmessage(it)
                            }
                        })
                    }
                    "Japonais"->{
                        toast("Japonais")
                        val progressdialog = indeterminateProgressDialog("In translated...")
                        FirebaseMlKitUtil.translateToAnyLanguage(messsageSource, FirebaseTranslateLanguage.FR, FirebaseTranslateLanguage.JA, onComplete = {
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
            }
            /*  setPositiveButton("OK") { dialog:DialogInterface, which:Int ->
                  toast("selection OK")
              }*/

            show()
        }
    }


   /* private val onItemClick = OnItemClickListener { item, view ->
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
    }*/

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_page_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var textAlert = ""

        if(Configuration.istranslateMessaActived){
            textAlert = if(Configuration.translete_language == FirebaseTranslateLanguage.EN)
                "Vous avez déja activer la traduction automatique en Anglais voulez-vous la désactiver ?"
            else
                "Vous avez déja activer la traduction automatique en Français voulez-vous la désactiver ?"
        }
        when (item?.itemId){
            R.id.id_menu_translete_all_input_en ->{

                if (!Configuration.istranslateMessaActived)
                    textAlert = "Voulez-vous Traduire tous les messages entrants et sortant en Anglais ?"

                val dialogBuilder = AlertDialog.Builder(this).apply {
                    setMessage(textAlert)
                        // if the dialog is cancelable
                        .setCancelable(false)
                        // positive button text and action
                        .setPositiveButton("OUI") { dialog, id ->
                            processTranslate(FirebaseTranslateLanguage.EN)
                            dialog.cancel()
                        }
                        // negative button text and action
                        .setNegativeButton("NON") { dialog, id ->
                            dialog.cancel()
                        }
                }
                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("Traduction automatique")
                // show alert dialog
                alert.show()
            }

            R.id.id_menu_translete_all_input_fr ->{
                if (!Configuration.istranslateMessaActived)
                    textAlert = "Voulez-vous Traduire tous les messages entrants et sortant en Français ?"
                else
                    textAlert = "Voulez-vous désactiver la traduction automatique?"
                val dialogBuilder = AlertDialog.Builder(this).apply {
                    setMessage(textAlert)
                        // if the dialog is cancelable
                        .setCancelable(false)
                        // positive button text and action
                        .setPositiveButton("OUI") { dialog, id ->
                            processTranslate(FirebaseTranslateLanguage.FR)
                            dialog.cancel()
                        }
                        // negative button text and action
                        .setNegativeButton("NON") { dialog, id ->
                            dialog.cancel()
                        }
                }
                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("Traduction automatique")
                // show alert dialog
                alert.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun processTranslate(language: Int) {
        if (!Configuration.istranslateMessaActived) {
            Toast.makeText(
                applicationContext,
                "Tous vos messages seront désormais en la langue sélectionnée",
                Toast.LENGTH_LONG
            ).show()
            Configuration.istranslateMessaActived = true
            Configuration.oldLanguage = Configuration.translete_language
            Configuration.translete_language = language
        } else {
            Configuration.istranslateMessaActived = false
            Toast.makeText(applicationContext, "Traduction automatique désactivée", Toast.LENGTH_LONG).show()
        }
    }

    //on désactive la traduction dè lors que l'utilisateur sort de la conversation
    override fun onBackPressed() {
        super.onBackPressed()
        Configuration.istranslateMessaActived = false
    }

}
