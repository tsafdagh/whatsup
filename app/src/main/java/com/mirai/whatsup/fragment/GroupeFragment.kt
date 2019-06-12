package com.mirai.whatsup.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.google.firebase.firestore.ListenerRegistration
import com.mirai.whatsup.R
import com.mirai.whatsup.utils.FireStoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_conversation.*
import kotlinx.android.synthetic.main.fragment_groupe.*
import org.jetbrains.anko.support.v4.startActivity



class GroupeFragment : Fragment() {


    private lateinit var userListenerRegistration: ListenerRegistration
    private var shouldInitrecycleView = true
    private lateinit var poepleSection: Section

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userListenerRegistration = FireStoreUtil.addGroupeListener(this.activity!!, onListen = {
            this.updateRecycleView(it)
        })
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groupe, container, false)
    }


    @SuppressLint("MissingSuperCall")
    override fun onDestroyView() {
        super.onDestroy()
        FireStoreUtil.removeListener(userListenerRegistration)
        shouldInitrecycleView = true
    }

    private fun updateRecycleView(items: List<Item>) {

        fun init(){
            recycle_view_groupe.apply {
                layoutManager = LinearLayoutManager(this@GroupeFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    poepleSection = Section(items)
                    add(poepleSection)
                    //setOnItemClickListener(onItemClick)
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

/*    private val onItemClick = OnItemClickListener{item, view ->
        if(item is PersonItem){
            startActivity<ChatActivity>(
                AppConstants.USER_NAME to item.person.name,
                AppConstants.USER_ID to item.userIdFirebase
            )

        }
    }*/

}
