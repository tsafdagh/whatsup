package com.mirai.whatsup.receycleView.item

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.opengl.Visibility
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout

import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.mirai.whatsup.AppConstants
import com.mirai.whatsup.ChatActivity
import com.mirai.whatsup.R
import com.mirai.whatsup.entities.TextMessage
import com.mirai.whatsup.entities.User
import com.mirai.whatsup.option.Configuration
import com.mirai.whatsup.utils.FireStoreUtil
import com.mirai.whatsup.utils.FirebaseMlKitUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_text_message_groupe.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.wrapContent
import java.text.SimpleDateFormat


class TextMessageItemGroup(
    val language: String,
    val message: TextMessage,
    val context: Context
) : Item() {
    var isSelectet = false
    private val colorTranslateText = Color.GRAY
    private val colorSrcText = Color.BLACK

    override fun bind(viewHolder: ViewHolder, position: Int) {

        var senderName: String = ""
        /*
        if (Configuration.istranslateMessaActived) {
            val progressdialog = ProgressDialog(context)
            progressdialog.setMessage(context.getString(R.string.text_in_translating))
            progressdialog.setCancelable(false)
            progressdialog.show()

            FirebaseMlKitUtil.translateToAnyLanguage(messageText, Configuration.oldLanguage, Configuration.translete_language,  onComplete = { stransletedMessage: String ->
                if (stransletedMessage.equals("-1")) {
                    Toast.makeText(context, context.getString(R.string.transleted_missing), Toast.LENGTH_LONG).show()
                    progressdialog.dismiss()
                } else {
                    progressdialog.dismiss()
                    viewHolder.textView_message_text_group.text = stransletedMessage
                    FireStoreUtil.getUserByUid(message.senderId, onComplete = {
                        viewHolder.textView_sender_name_txt.text = it.name
                    })

                    val dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
                    viewHolder.textView_message_time_groupe.text = dateFormat.format(message.time)
                    setMessageRootGravity(viewHolder)

                }
            })
        } else {
            viewHolder.textView_message_text_group.text = messageText
            FireStoreUtil.getUserByUid(message.senderId, onComplete = {
                viewHolder.textView_sender_name_txt.text = it.name
                val dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
                viewHolder.textView_message_time_groupe.text = dateFormat.format(message.time)
                setMessageRootGravity(viewHolder)
            })

        }*/
        val dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.textView_message_time_groupe.text = dateFormat.format(message.time)

        var origineLanguageCode = "?"
        // on recupère zt on affiche le nom de de celui qui a envoyer le message
        FireStoreUtil.getUserByUid(message.senderId, onComplete = {
            senderName = "me"
            // on controle si le message ne provient pas del'utiliisateur couran
            if (FirebaseAuth.getInstance().currentUser?.uid != message.senderId)
                senderName = it.name

            viewHolder.textView_sender_name_txt.text = senderName
        })
        FirebaseMlKitUtil.translateMsg(language, message.text, onComplete = { stransletedMessage: String ->
            viewHolder.textView_message_text_group.text = stransletedMessage
            if (stransletedMessage.equals(message.text, true)) {
                viewHolder.textView_message_text_group.setTextColor(colorSrcText)
                viewHolder.textView_message_text_group.setTypeface(null, Typeface.NORMAL)

            } else {
                viewHolder.textView_message_text_group.setTextColor(colorTranslateText)
                viewHolder.textView_message_text_group.setTypeface(null, Typeface.ITALIC)

            }
            // recuperation de la langue du texte
            val languageIdentifier = FirebaseNaturalLanguage
                .getInstance()
                .languageIdentification
            languageIdentifier.identifyLanguage(stransletedMessage)
                .addOnSuccessListener { languageCode ->
                    origineLanguageCode = showLanguageCode(languageCode)
                    if (!languageCode.equals("und", true)) {
                        var code = languageCode
                        if (languageCode.equals("en", true)) {
                            code = "fr"
                        } else if (languageCode.equals("fr", true)) {
                            code = "en"
                        }
                        viewHolder.button_translate_item_groupe.text = code
                    } else {
                        viewHolder.button_translate_item_groupe.text = "?"
                    }
                }
                .addOnFailureListener(
                    object : OnFailureListener {
                        override fun onFailure(e: Exception) {
                            viewHolder.button_translate_item_groupe.text = "?"
                        }
                    })
        })
        viewHolder.button_translate_item_groupe.setOnClickListener {
            val langCode = viewHolder.button_translate_item_groupe.text.toString()
            val msg = viewHolder.textView_message_text_group.text.toString()
            var srcCodeLanguage = -1
            var destCodeLanguage = -1
            var newCode = "en"
            when (langCode) {
                "fr" -> {
                    srcCodeLanguage = FirebaseTranslateLanguage.EN
                    destCodeLanguage = FirebaseTranslateLanguage.FR
                    newCode = "en"
                }
                "en" -> {
                    srcCodeLanguage = FirebaseTranslateLanguage.FR
                    destCodeLanguage = FirebaseTranslateLanguage.EN
                    newCode = "fr"
                }
            }
            if (srcCodeLanguage == -1 || destCodeLanguage == -1) {
                Toast.makeText(context, "Langue non prise en charge", Toast.LENGTH_SHORT).show()
            } else {
                val options = FirebaseTranslatorOptions.Builder()
                    .setSourceLanguage(srcCodeLanguage)
                    .setTargetLanguage(destCodeLanguage)
                    .build()
                val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
                translator.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        translator.translate(msg)
                            .addOnSuccessListener { translatedText ->
                                viewHolder.textView_message_text_group.text = translatedText
                                viewHolder.button_translate_item_groupe.text = newCode
                                viewHolder.textView_message_text_group.setTextColor(colorTranslateText)
                            }
                            .addOnFailureListener { exception ->
                                viewHolder.textView_message_text_group.text = msg
                                viewHolder.textView_message_text_group.setTextColor(colorSrcText)

                            }
                    }
                    .addOnFailureListener { exception ->
                        viewHolder.textView_message_text_group.text = msg
                        viewHolder.textView_message_text_group.setTextColor(colorSrcText)
                    }
            }
        }
        viewHolder.button_no_translate_item_group.setOnClickListener {
            viewHolder.textView_message_text_group.text = message.text
            viewHolder.textView_message_text_group.setTextColor(colorSrcText)
            viewHolder.textView_message_text_group.setTypeface(null, Typeface.NORMAL)
            viewHolder.button_no_translate_item_group.visibility = View.GONE

            viewHolder.button_translate_item_groupe.text = origineLanguageCode
        }

        if(language != AppConstants.NO_LANGUAGE){
            viewHolder.button_no_translate_item_group.visibility = View.VISIBLE
        }else{
            viewHolder.button_no_translate_item_group.visibility = View.GONE
        }


        setMessageRootGravity(viewHolder)
    }

    private fun showLanguageCode(languageCode: String): String {
        return when (languageCode) {
            "und" -> "?"
            "en" -> "fr"
            "fr" -> "en"
            else -> languageCode
        }
    }

    private fun setMessageRootGravity(viewHolder: ViewHolder) {
        if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            viewHolder.message_root.apply {
                backgroundResource = R.drawable.rect_round_primary_color
                val Iparams = FrameLayout.LayoutParams(wrapContent, wrapContent, Gravity.END)
                this.layoutParams = Iparams
            }
        } else {
            viewHolder.message_root.apply {
                backgroundResource = R.drawable.rect_round_white
                val Iparams = FrameLayout.LayoutParams(wrapContent, wrapContent, Gravity.START)
                this.layoutParams = Iparams
            }
        }
    }


    override fun getLayout() = R.layout.item_text_message_groupe

    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if (other !is TextMessageItem)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as? TextMessageItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}