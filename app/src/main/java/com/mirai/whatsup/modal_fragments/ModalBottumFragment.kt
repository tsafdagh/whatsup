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


class ModalBottumFragment(): BottomSheetDialogFragment() {
    private lateinit var userListenerRegistration: ListenerRegistration
    private var shouldInitrecycleView = true
    private lateinit var poepleSection: Section


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userListenerRegistration = FireStoreUtil.addSearchUserListener("",
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

        fun init(){
            recycle_view_peaple.apply {
                layoutManager = LinearLayoutManager(this@ModalBottumFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    poepleSection = Section(items)
                    add(poepleSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitrecycleView = false
        }

        fun updateItems() = poepleSection.update(items)

        if(shouldInitrecycleView)
            init()
        else
            updateItems()

    }

    private val onItemClick = OnItemClickListener{item, view ->
        view.setBackgroundColor(Color.parseColor("#AA574B"))
        if(item is PersonItem){
            toast("Utilisateur ajouter  ${item.userIdFirebase}")
            ParamModalFragment.listIdUserForGroup.add(item.userIdFirebase)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        toast("I' destroyed")
    }

}