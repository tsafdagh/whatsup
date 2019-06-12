package com.mirai.whatsup.receycleView.item

import android.content.Context
import com.mirai.whatsup.R
import com.mirai.whatsup.entities.ChatGroup
import com.mirai.whatsup.glide.GlideApp
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_person.*

class GroupeItem(val chatGroupe: ChatGroup,
                 val chatGroupeId: String,
                 val context: Context) : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.textView_name.text =chatGroupe.groupeName
        viewHolder.textView_bio.text = chatGroupe.groupeDescription
        if(chatGroupe.groupIcon != ""){
            GlideApp.with(context)
                .load(chatGroupe.groupIcon)
                .placeholder(R.drawable.icon_groupe_chat)
                .into(viewHolder.imageView_profile_picture)
        }

    }

    override fun getLayout() = R.layout.item_person

}