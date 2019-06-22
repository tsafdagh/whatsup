package com.mirai.whatsup.fragment


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import com.google.firebase.firestore.ListenerRegistration
import com.mirai.whatsup.AppConstants
import com.mirai.whatsup.ChatActivity

import com.mirai.whatsup.R
import com.mirai.whatsup.receycleView.item.PersonItem
import com.mirai.whatsup.utils.FireStoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_conversation.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast


class ConversationFragment : Fragment() {

    private lateinit var userListenerRegistration: ListenerRegistration
    private var shouldInitrecycleView = true
    private lateinit var poepleSection: Section

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userListenerRegistration = FireStoreUtil.addSearchUserListener("",
            this@ConversationFragment.context!!
            ,
            onListen = {
                updateRecycleView(it)
            }
        )

        setHasOptionsMenu(true)

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
                layoutManager = LinearLayoutManager(this@ConversationFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    poepleSection = Section(items)
                    add(poepleSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitrecycleView = false
        }

        fun updateItems() = poepleSection.update(items)

        if (shouldInitrecycleView) {
            try {
                init()
            } catch (e: Exception) {
                Log.e("Groupefragent", "Erreur Null: " + e.message)
            }
        } else
            updateItems()

    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is PersonItem) {
            startActivity<ChatActivity>(
                AppConstants.USER_NAME to item.person.name,
                AppConstants.USER_ID to item.userIdFirebase
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.search_menu, menu)

        val mySreachViewItem = menu!!.findItem(R.id.id_search_view_conversation_fragment)
        val mySearchView = mySreachViewItem.actionView as SearchView
        mySearchView.isSubmitButtonEnabled = true
        mySearchView.queryHint = "Rechercher..."
        mySearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchingText: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(searchingText: String?): Boolean {
                userListenerRegistration = FireStoreUtil.addSearchUserListener(searchingText!!,
                    this@ConversationFragment.context!!
                    ,
                    onListen = {
                        updateRecycleView(it)
                    }
                )
                return true
            }

        })

    }


}
