package com.mirai.whatsup.receycleView.item

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.mirai.whatsup.R
import com.mirai.whatsup.entities.TextMessage
import com.mirai.whatsup.option.Configuration
import com.mirai.whatsup.utils.FirebaseMlKitUtil
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_text_message.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.wrapContent
import java.text.SimpleDateFormat
import java.time.format.TextStyle

class TextMessageItem(
    val language: String,
    val message: TextMessage,
    val context: Context
) : MessageItem(message) {
    private val colorTranslateText= Color.parseColor("#49322D")
    private val colorSrcText=Color.BLACK
    //var isSelectet = false
    override fun bind(viewHolder: ViewHolder, position: Int) {

        var origineLanguageCode="?"
        FirebaseMlKitUtil.translateMsg(language,message.text, onComplete = { stransletedMessage: String ->
            viewHolder.textView_message_text.text=stransletedMessage
            if(stransletedMessage.equals(message.text,true)){
                viewHolder.textView_message_text.setTextColor(colorSrcText)
            }
            else{
                viewHolder.textView_message_text.setTextColor(colorTranslateText)
                viewHolder.textView_message_text.setTypeface(null, Typeface.ITALIC)
            }
            // recuperation de la langue du texte
            val languageIdentifier = FirebaseNaturalLanguage
                .getInstance()
                .languageIdentification
            languageIdentifier.identifyLanguage(stransletedMessage)
                .addOnSuccessListener { languageCode ->
                    origineLanguageCode=showLanguageCode(languageCode)
                    if(!languageCode.equals("und",true)){
                        var code=languageCode
                        if(languageCode.equals("en",true)){
                            code="fr"
                        }
                        else if(languageCode.equals("fr",true)){
                            code="en"
                        }
                        viewHolder.button_translate_item.text=code
                    }
                    else{
                        viewHolder.button_translate_item.text="?"
                    }
                }
                .addOnFailureListener { viewHolder.button_translate_item.text="?" }
        })

        viewHolder.button_translate_item.setOnClickListener {
            val langCode=viewHolder.button_translate_item.text.toString()
            val msg=viewHolder.textView_message_text.text.toString()
            var srcCodeLanguage=-1
            var destCodeLanguage=-1
            var newCode="en"
            when(langCode){
                "fr"->{
                    srcCodeLanguage= FirebaseTranslateLanguage.EN
                    destCodeLanguage=FirebaseTranslateLanguage.FR
                    newCode="en"
                }
                "en"->{
                    srcCodeLanguage=FirebaseTranslateLanguage.FR
                    destCodeLanguage=FirebaseTranslateLanguage.EN
                    newCode="fr"
                }
            }
            if(srcCodeLanguage==-1 || destCodeLanguage==-1){
                Toast.makeText(context,"Langue non prise en charge",Toast.LENGTH_SHORT).show()
            }
            else{
                val options = FirebaseTranslatorOptions.Builder()
                    .setSourceLanguage(srcCodeLanguage)
                    .setTargetLanguage(destCodeLanguage)
                    .build()
                val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
                translator.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        translator.translate(msg)
                            .addOnSuccessListener { translatedText ->
                                viewHolder.textView_message_text.text=translatedText
                                viewHolder.button_translate_item.text=newCode
                                viewHolder.textView_message_text.setTextColor(colorTranslateText)
                            }
                            .addOnFailureListener { exception ->
                                viewHolder.textView_message_text.text=msg
                                viewHolder.textView_message_text.setTextColor(colorSrcText)

                            }
                    }
                    .addOnFailureListener { exception ->
                        viewHolder.textView_message_text.text=msg
                        viewHolder.textView_message_text.setTextColor(colorSrcText)
                    }
            }
        }
        viewHolder.button_no_translate_item.setOnClickListener {
            viewHolder.textView_message_text.text=message.text
            viewHolder.textView_message_text.setTextColor(colorSrcText)
            viewHolder.button_translate_item.text=origineLanguageCode
        }
        super.bind(viewHolder, position)

        /*if (Configuration.istranslateMessaActived) {
            val progressdialog = ProgressDialog(context)
            progressdialog.setMessage(context.getString(R.string.text_in_translating))
            progressdialog.setCancelable(false)
            progressdialog.show()


            FirebaseMlKitUtil.translateToAnyLanguage(messageText, Configuration.oldLanguage,Configuration.translete_language, onComplete = { stransletedMessage: String ->
                if (stransletedMessage.equals("-1")) {
                    Toast.makeText(context, context.getString(R.string.transleted_missing), Toast.LENGTH_LONG).show()
                    progressdialog.dismiss()
                } else {
                    progressdialog.dismiss()
                    viewHolder.textView_message_text.text = stransletedMessage
                    super.bind(viewHolder, position)

                }
            })
        } else {
            viewHolder.textView_message_text.text = messageText
            super.bind(viewHolder, position)
        }*/
    }

    private fun showLanguageCode(languageCode: String): String {
        return when(languageCode){
            "und"->"?"
            "en"->"fr"
            "fr"->"en"
            else->languageCode
        }
    }

    private fun setTimetext(viewHolder: ViewHolder) {
        val dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.textView_message_time.text = dateFormat.format(message.time)
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

    override fun getLayout() = R.layout.item_text_message

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