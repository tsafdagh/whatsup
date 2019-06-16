package com.mirai.whatsup.receycleView.item

import android.app.ProgressDialog
import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout

import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
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
    val message: TextMessage,
    val context: Context
): Item(){
     override fun bind(viewHolder: ViewHolder, position: Int) {

        var messageText = message.text
        if (Configuration.istranslateMessaToEnglishActived) {
            val progressdialog = ProgressDialog(context)
            progressdialog.setMessage(context.getString(R.string.text_in_translating))
            progressdialog.setCancelable(false)
            progressdialog.show()

            FirebaseMlKitUtil.translateToEnglish(messageText, onComplete = { stransletedMessage: String ->
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