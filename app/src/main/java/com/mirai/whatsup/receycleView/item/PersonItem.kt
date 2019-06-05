package com.mirai.whatsup.receycleView.item

import android.content.Context
import com.mirai.whatsup.R
import com.mirai.whatsup.entities.User
import com.mirai.whatsup.glide.GlideApp
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_person.*

class PersonItem(val person: User,
                 val userIdFirebase: String,
                 private val context: Context): Item(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_name.text = person.name
                viewHolder.textView_bio.text = person.bio
        if(person.profilePicturePath != null){
            GlideApp.with(context)
                .load(person.profilePicturePath)
                .placeholder(R.drawable.ic_account_box_black_24dp)
                .into(viewHolder.imageView_profile_picture)
        }

    }

    override fun getLayout()= R.layout.item_person
}