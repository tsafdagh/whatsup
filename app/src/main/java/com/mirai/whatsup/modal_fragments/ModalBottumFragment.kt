package com.mirai.whatsup.modal_fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.ListenerRegistration
import com.mirai.whatsup.R
import com.mirai.whatsup.receycleView.item.PersonItem
import com.mirai.whatsup.utils.FireStoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_conversation.*
import org.jetbrains.anko.support.v4.toast


class ModalBottumFragment() : BottomSheetDialogFragment() {
    private lateinit var userListenerRegistration: ListenerRegistration
    private var shouldInitrecycleView = true
    private lateinit var poepleSection: Section

    // cet objet va contenir la liste des mebres du groupe qui auront été sélectionnés à la création du groupe
    private val memberOfGroupe = arrayListOf<ModalSelectedMember>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        userListenerRegistration = FireStoreUtil.addSearchUserListenerForcreatingGroupe("",
            this@ModalBottumFragment.context!!
            ,
            onListen = {
                updateRecycleView(it)
            }
        )

        ParamModalFragment.listIdUserForGroup.clear()
        return inflater.inflate(R.layout.fragment_conversation, container, false)
    }

    @SuppressLint("MissingSuperCall")
    override fun onDestroyView() {
        super.onDestroy()
        FireStoreUtil.removeListener(userListenerRegistration)
        shouldInitrecycleView = true
    }

    private fun updateRecycleView(items: List<Item>) {

        fun init() {
            recycle_view_peaple.apply {
                layoutManager = LinearLayoutManager(this@ModalBottumFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    poepleSection = Section(items)
                    add(poepleSection)
                    //setOnItemClickListener(onItemClick)
                }
            }
            shouldInitrecycleView = false
        }

        fun updateItems() = poepleSection.update(items)

        if (shouldInitrecycleView)
            init()
        else
            updateItems()

    }
    /*private val onItemClick = OnItemClickListener { item, view ->
        if (item is PersonItem) {
            if (this.memberOfGroupe.isNotEmpty()) {
                *//*On parcour la liste des membres déja ajoutés dans le groupe
                * si l'utilisateur courant avait déja été ajouter on l'enlève et on déselectionne
                * va vue*//*

                for (i in 0..memberOfGroupe.size) {
                    var tmp_item = memberOfGroupe[i]
                    if (tmp_item.view == view) {
                        memberOfGroupe.remove(tmp_item)
                        view.setBackgroundColor(Color.TRANSPARENT)
                        toast("retrait du membre déja ajouter")
                        break
                    } else {
                        //ParamModalFragment.listIdUserForGroup.add(item.userIdFirebase)
                        memberOfGroupe.add(ModalSelectedMember(item.userIdFirebase, view))
                        view.setBackgroundColor(Color.parseColor("#AA574B"))
                        toast("Ajout d'un nouveau membre")
                        break
                    }

                }
            }else{
               // si la liste des membres du groupe est vide on ajoute l'utilisateur sélectionner'
                memberOfGroupe.add(ModalSelectedMember(item.userIdFirebase, view))
                view.setBackgroundColor(Color.parseColor("#AA574B"))
                toast("Ajout d'un nouveau membre")
            }

        }
    }
*/
    override fun onDestroy() {
        super.onDestroy()
        //ParamModalFragment.listIdUserForGroup.clear()
        for (tmp_item in this.memberOfGroupe) {
            //ParamModalFragment.listIdUserForGroup.add(tmp_item.uidSelectedMember)
        }
    }

    data class ModalSelectedMember(var uidSelectedMember: String, var view: View)

}