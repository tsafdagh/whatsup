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
import com.mirai.whatsup.ChatGroupActivity
import com.mirai.whatsup.R
import com.mirai.whatsup.receycleView.item.GroupeItem
import com.mirai.whatsup.utils.FireStoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_groupe.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast


class GroupeFragment : Fragment() {


    private lateinit var groupeListenerRegistration: ListenerRegistration
    private var shouldInitrecycleView = true
    private lateinit var poepleSection: Section

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        groupeListenerRegistration = FireStoreUtil.addSearchGroupeListener("",
            this@GroupeFragment.context!!
            ,
            onListen = {
                updateRecycleView(it)
            }
        )
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groupe, container, false)
    }


    @SuppressLint("MissingSuperCall")
    override fun onDestroyView() {
        super.onDestroy()
        FireStoreUtil.removeListener(groupeListenerRegistration)
        shouldInitrecycleView = true
    }

    private fun updateRecycleView(items: List<Item>) {

        fun init() {
            recycle_view_groupe.apply {
                layoutManager = LinearLayoutManager(this@GroupeFragment.context)
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
           }catch (e: Exception){
               Log.e("Groupefragent", "Erreur Null: "+e.message)
           }
        } else
            updateItems()

    }

    private val onItemClick = OnItemClickListener { item, view ->
        if (item is GroupeItem) {
            startActivity<ChatGroupActivity>(
                AppConstants.ID_GROUPE to item.chatGroupeId,
                AppConstants.NOM_GROUPE to item.chatGroupe.groupeName,
                AppConstants.NOMBRE_MEMBRE_GROUPE to item.chatGroupe.members?.size.toString()
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
                groupeListenerRegistration = FireStoreUtil.addSearchGroupeListener(searchingText!!,
                    this@GroupeFragment.context!!
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
