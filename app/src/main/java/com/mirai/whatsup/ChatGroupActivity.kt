package com.mirai.whatsup

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.RingtoneManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.mirai.whatsup.entities.ImageMessage
import com.mirai.whatsup.entities.MessageType
import com.mirai.whatsup.entities.TextMessage
import com.mirai.whatsup.entities.User
import com.mirai.whatsup.option.Configuration
import com.mirai.whatsup.receycleView.item.ImageMessageItemGroup
import com.mirai.whatsup.receycleView.item.TextMessageItemGroup
import com.mirai.whatsup.utils.FireStoreUtil
import com.mirai.whatsup.utils.FirebaseMlKitUtil
import com.mirai.whatsup.utils.StorageUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat_group.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.util.*


private const val RC_SELECT_IMAGE = 2

class ChatGroupActivity : AppCompatActivity() {


    private lateinit var currentGroupeUID: String
    private var sizeOfmember: Int = 0

    private lateinit var messageListenerRegistration: ListenerRegistration
    private var shouldInitRecycleView = true
    private lateinit var messageSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_group)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.NOM_GROUPE)
        sizeOfmember = intent.getStringExtra(AppConstants.NOMBRE_MEMBRE_GROUPE).toInt()
        currentGroupeUID = intent.getStringExtra(AppConstants.ID_GROUPE)
        //on désactive la traduction automatique
        Configuration.istranslateMessaActived = false

        messageListenerRegistration = FireStoreUtil.addGroupeChatMessagesListener(currentGroupeUID, this, onListner = {
            updateRecycleView(it)
        })
        imageView_send_groupe.setOnClickListener {
            var textMessage = editText_message_groupe.text.toString()
            if (Configuration.istranslateMessaActived) {
                val progressdialog = indeterminateProgressDialog("Traduction en cours...")
                FirebaseMlKitUtil.translateToAnyLanguage(
                    textMessage,
                    Configuration.oldLanguage,
                    Configuration.translete_language,
                    onComplete = { stransletedMessage: String ->
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
                            editText_message_groupe.setText("")
                            FireStoreUtil.sendGroupeMessage(messageText, currentGroupeUID)
                        }
                    })

            } else {
                val messageText = TextMessage(
                    textMessage,
                    Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser!!.uid, MessageType.TEXT
                )
                editText_message_groupe.setText("")
                FireStoreUtil.sendGroupeMessage(messageText, currentGroupeUID)
            }
        }

        fab_send_image_groupe.setOnClickListener {

            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }

            startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), RC_SELECT_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            val selectedimagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(contentResolver, selectedimagePath)
            val outputStream = ByteArrayOutputStream()

            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadMessageImageFromByteArray(selectedImageBytes, onSuccess = { imagepath: String ->
                val messageToSend = ImageMessage(
                    imagepath, Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser!!.uid
                )

                FireStoreUtil.sendGroupeMessage(messageToSend, currentGroupeUID)
            })
        }
    }

    private fun updateRecycleView(messages: List<Item>) {
        fun init() {
            recycler_view_messages_groupe.apply {
                layoutManager = LinearLayoutManager(this@ChatGroupActivity)
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

        recycler_view_messages_groupe.scrollToPosition((recycler_view_messages_groupe.adapter?.itemCount ?: 1) - 1)
    }

    private val onItemClick = OnItemClickListener { item, view ->

        val progressdialog = ProgressDialog(this)
        progressdialog.setMessage("Chagement")
        progressdialog.setCancelable(false)
        progressdialog.show()
        if (item is TextMessageItemGroup) {

            //on recherche le nom de l'utilisateur ayant envoyer le message
            FireStoreUtil.getUserByUid(item.message.senderId, onComplete = {
                progressdialog.dismiss()
                val items = arrayOf("Demarer un chat privé avec " + it.name, "Traduire", "Marquer le message")
                val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustomStyle))
                view.setBackgroundColor(Color.TRANSPARENT)
                with(builder)
                {
                    setTitle("Choisir une action")
                    setItems(items) { dialog, which ->
                        when (items[which]) {
                            "Traduire" -> {
                                showAnaylanguageTranslatedDialog(item.message.text)
                                if (!item.isSelectet) {
                                    view.setBackgroundColor(Color.RED)
                                    item.isSelectet = true
                                } else {
                                    item.isSelectet = false
                                }

                            }
                            "Marquer le message" -> {
                                view.setBackgroundColor(Color.YELLOW)
                            }
                            "Demarer un chat privé avec " + it.name -> {
                               openChatActivity(it, item.message.senderId)
                            }
                        }
                    }

                    show()
                }
            })

        } else {

            val item = item as ImageMessageItemGroup
            FireStoreUtil.getUserByUid(item.message.senderId, onComplete = {
                progressdialog.dismiss()
                val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustomStyle))

                with(builder)
                {
                    setTitle("Démarrer un nouveau chat ")
                    setMessage("Démarer le chat avec avec " + it.name)
                    setPositiveButton("Oui", DialogInterface.OnClickListener { dialog, which ->
                        openChatActivity(it, item.message.senderId)
                    })
                    setNegativeButton("Non", DialogInterface.OnClickListener { dialog, which ->
                        // Do something when user press the positive button
                    })
                    show()
                }
            })
        }

        true
    }

    private fun openChatActivity(it: User, senderId: String) {
       // startActivity<ChatActivity>(AppConstants.USER_NAME to it.name, AppConstants.USER_ID to senderId)
        val myIntent = Intent(this, ChatActivity::class.java)
        myIntent.putExtra(AppConstants.USER_NAME , it.name)
        myIntent.putExtra(AppConstants.USER_ID , senderId)

        startActivity(myIntent)
        finish()
    }

    private fun showAnaylanguageTranslatedDialog(messsageSource: String) {
        val items = arrayOf("Anglais", "Français", "Japonais")
        val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogCustomStyle))
        with(builder)
        {
            setTitle("Choisir La langue de traduction")
            setItems(items) { dialog, which ->
                when (items[which]) {
                    "Anglais" -> {
                        toast("Anglais")
                        val progressdialog = indeterminateProgressDialog("Traduction en cours...")
                        FirebaseMlKitUtil.translateToAnyLanguage(
                            messsageSource,
                            FirebaseTranslateLanguage.FR,
                            FirebaseTranslateLanguage.EN,
                            onComplete = {
                                if (it.equals("-1")) {
                                    toast("Traduction failled")
                                    progressdialog.dismiss()
                                } else {
                                    progressdialog.dismiss()
                                    showTranslatedmessage(it)
                                }
                            })
                    }
                    "Français" -> {
                        toast("Francais")
                        val progressdialog = indeterminateProgressDialog("In translate...")
                        FirebaseMlKitUtil.translateToAnyLanguage(
                            messsageSource,
                            FirebaseTranslateLanguage.EN,
                            FirebaseTranslateLanguage.FR,
                            onComplete = {
                                if (it.equals("-1")) {
                                    toast("Traduction échouée")
                                    progressdialog.dismiss()
                                } else {
                                    progressdialog.dismiss()
                                    showTranslatedmessage(it)
                                }
                            })
                    }
                    "Japonais" -> {
                        toast("Japonais")
                        val progressdialog = indeterminateProgressDialog("In translated...")
                        FirebaseMlKitUtil.translateToAnyLanguage(
                            messsageSource,
                            FirebaseTranslateLanguage.FR,
                            FirebaseTranslateLanguage.JA,
                            onComplete = {
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
            setNegativeButton("FERMER") { dialog, which ->
                // Do something when user press the positive button
            }

            show()
        }
    }

    fun showTranslatedmessage(text: String) {

        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Tanslated message")
            setMessage(text)
            setPositiveButton("OK") { dialog, which ->
                // Do something when user press the positive button
            }
            show()
        }
    }


    /*  fun showTranslatedmessage(text: String) {

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
      }*/


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_page_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var textAlert = ""

        if (Configuration.istranslateMessaActived) {
            textAlert = if (Configuration.translete_language == FirebaseTranslateLanguage.EN)
                "Vous avez déja activer la traduction automatique en Anglais voulez-vous la désactiver ?"
            else
                "Vous avez déja activer la traduction automatique en Français voulez-vous la désactiver ?"
        }
        when (item?.itemId) {
            R.id.id_menu_translete_all_input_en -> {

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

            R.id.id_menu_translete_all_input_fr -> {
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
