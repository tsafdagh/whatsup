package com.mirai.whatsup.receycleView.item

import android.content.Context
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.mirai.whatsup.R
import com.mirai.whatsup.entities.User
import com.mirai.whatsup.glide.GlideApp
import com.mirai.whatsup.modal_fragments.ParamModalFragment
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_person.*
import kotlinx.android.synthetic.main.item_person.imageView_profile_picture
import kotlinx.android.synthetic.main.item_person.textView_bio
import kotlinx.android.synthetic.main.item_person.textView_name
import kotlinx.android.synthetic.main.item_persone_create_groupe.*
import org.jetbrains.anko.toast

class SelectedPersonItem (val person: User,
                          val userIdFirebase: String,
                          private val context: Context
): Item(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_name.text = person.name
        viewHolder.textView_bio.text = person.bio
        if(person.profilePicturePath != null){
            GlideApp.with(context)
                .load(person.profilePicturePath)
                .transform(CircleCrop())
                .placeholder(R.drawable.ic_account_box_black_24dp)
                .into(viewHolder.imageView_profile_picture)
        }
        viewHolder.id_chekbox.setOnCheckedChangeListener{buttonView, ischecked->
            if(ischecked){
                context.toast("utilisateur ajouter: "+userIdFirebase)
                ParamModalFragment.listIdUserForGroup.add(userIdFirebase)
            }else{
                context.toast("utilisateur retirer: "+userIdFirebase)
                ParamModalFragment.listIdUserForGroup.remove(userIdFirebase)
            }
        }

    }
    override fun getLayout()= R.layout.item_persone_create_groupe
}