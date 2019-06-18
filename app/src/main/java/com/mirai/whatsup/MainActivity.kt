package com.mirai.whatsup

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.mirai.whatsup.fragment.ConversationFragment
import com.mirai.whatsup.fragment.GroupeFragment
import com.mirai.whatsup.fragment.MyAccountFragment
import com.mirai.whatsup.option.Configuration
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.navigation)

        replaceFragment(ConversationFragment())
        navView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_people -> {
                    replaceFragment(ConversationFragment())
                    true
                }

                R.id.item_groupes -> {
                    replaceFragment(GroupeFragment())
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_layout, fragment)
            commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId


        when (itemId) {
            R.id.id_menu_mon_profil ->
                replaceFragment(MyAccountFragment())
            R.id.id_item_creer_group ->{
                toast("creer un groupe")
                startActivity<CreationGroupe>()

            }
        }
        return super.onOptionsItemSelected(item)
    }
}
