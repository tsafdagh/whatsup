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
import com.mirai.whatsup.fragment.MyAccountFragment
import com.mirai.whatsup.option.Configuration
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

                R.id.navigation_my_account -> {
                    replaceFragment(MyAccountFragment())
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
            R.id.id_select_image ->
                //TO DO
                toast("menu mon profils")
            R.id.id_menu_translete_text -> {

                var textAlert =""
                if (!Configuration.istranslateMessaToEnglishActived)
                    textAlert = "Voulez-vous Traduire les messages entrants et sortant?"
                else
                    textAlert = "Voulez-vous désactiver la traduction automatique?"
                val dialogBuilder = AlertDialog.Builder(this).apply {
                    setMessage(textAlert)
                        // if the dialog is cancelable
                        .setCancelable(false)
                        // positive button text and action
                        .setPositiveButton("OUI", DialogInterface.OnClickListener { dialog, id ->
                            processTranslate()
                            dialog.cancel()
                        })
                        // negative button text and action
                        .setNegativeButton("NON", DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                        })
                }
                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("Traduction automatique")
                // show alert dialog
                alert.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun processTranslate() {
        if (!Configuration.istranslateMessaToEnglishActived) {
            Toast.makeText(
                applicationContext,
                "Tous vos messages seront désormais en anglais",
                Toast.LENGTH_LONG
            ).show()
            Configuration.istranslateMessaToEnglishActived = true
        } else {
            Configuration.istranslateMessaToEnglishActived = false
            Toast.makeText(applicationContext, "Traduction anglaise désactivée", Toast.LENGTH_LONG).show()
        }
    }
}
