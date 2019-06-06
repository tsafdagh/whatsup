package com.mirai.whatsup.receycleView.item

import android.content.Context
import com.mirai.whatsup.R
import com.mirai.whatsup.entities.TextMessage
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

class TextMessageItem(
    val message: TextMessage,
    val context: Context
) : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout() = R.layout.item_text_message
}