package com.mirai.whatsup.receycleView.item

import android.content.Context
import com.mirai.whatsup.R
import com.mirai.whatsup.entities.ImageMessage
import com.mirai.whatsup.entities.User
import com.mirai.whatsup.glide.GlideApp
import com.mirai.whatsup.utils.FireStoreUtil
import com.mirai.whatsup.utils.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_image_message.*
import kotlinx.android.synthetic.main.item_image_message_groupe.*

class ImageMessageItemGroup(val message:ImageMessage,
                            val context:Context):MessageItem(message) {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        GlideApp.with(context)
            .load(StorageUtil.pathToReference(message.imagePath))
            .placeholder(R.drawable.ic_gallery)
            .into(viewHolder.imageView_message_image_groupe)

        FireStoreUtil.getUserByUid(message.senderId, onComplete = {
            viewHolder.textView_sender_name_groupe.text = it.name
        })
    }

    override fun getLayout()= R.layout.item_image_message_groupe

    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if (other !is ImageMessageItemGroup)
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