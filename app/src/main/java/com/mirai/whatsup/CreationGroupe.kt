package com.mirai.whatsup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore

import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.facebook.common.util.UriUtil
import com.google.firebase.auth.FirebaseAuth

import com.mirai.whatsup.glide.GlideApp
import com.mirai.whatsup.modal_fragments.ModalBottumFragment
import com.mirai.whatsup.modal_fragments.ParamModalFragment
import com.mirai.whatsup.receycleView.item.PersonItem
import com.mirai.whatsup.utils.FireStoreUtil
import com.mirai.whatsup.utils.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_creation_groupe.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreationGroupe : AppCompatActivity() {

    val CAMERA_REQUEST_CODE = 0
    val RC_SELECT_IMAGE = 5
    //Permission code
    private val PERMISSION_CODE_GALERY = 1001;
    var imageGroupeUri: Uri? = null

    private val listeDesMembreDuGroupe = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creation_groupe)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imgAvatar.setOnClickListener {

            val textAlert = "Ouvrir à partir de la gallerie?"
            val dialogBuilder = android.app.AlertDialog.Builder(this).apply {
                setMessage(textAlert)
                    // if the dialog is cancelable
                    .setCancelable(true)
                    // positive button text and action
                    .setPositiveButton("OUI") { dialog, id ->
                        openGallery()
                        dialog.cancel()
                    }
                    // negative button text and action
                    .setNegativeButton("NON") { dialog, id ->
                        //dispatchTakePictureIntent()
                        dialog.cancel()
                    }
            }
            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("Sélection d'image")
            // show alert dialog
            alert.show()
        }
        ParamModalFragment.listIdUserForGroup.clear()
        btn_selection_membres.setOnClickListener {
            if (verifierChampOk()) {
                val myModal = ModalBottumFragment()
                myModal.show(supportFragmentManager, "MODAL")

                //FireStoreUtil.getListOfUser(this, onListen = { createChoisAlertDialog(it) })

                btn_creer_groupe.visibility = View.VISIBLE

                if (listeDesMembreDuGroupe.size != 0) {
                    btn_creer_groupe.visibility = View.VISIBLE
                } else {
                    // btn_creer_groupe.visibility = View.GONE
                    val snackbar = Snackbar.make(
                        id_vue_creer_groupe,
                        "Vous devez sélectionner des membres",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.show()
                }

            }
        }

        btn_creer_groupe.setOnClickListener {
            if (ParamModalFragment.listIdUserForGroup.size != 0 && imageGroupeUri != null)
                sendDataToFireBAse()
            else {
                val snackbar = Snackbar.make(
                    id_vue_creer_groupe,
                    "sélectionnez des membres du groupe et/ou  une image",
                    Snackbar.LENGTH_LONG
                )
                snackbar.show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e(CreationGroupe::class.java.name, ex.toString())
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.mirai.whatsup.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            }
        }
    }

    var currentPhotoPath: String = ""

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    /* private fun createChoisAlertDialog(usersList: List<Item>) {

         // on nétoie la liste précedement créee
         listeDesMembreDuGroupe.clear()

         // on a besoin des noms et des ids pour sélectionner les membres et creer le groupe creer un groupe
         val listNames = ArrayList<String>()
         var listIds = ArrayList<String>()

         for (i in 0 until usersList.size) {
             var tmp = usersList[i] as PersonItem
             listNames.add(tmp.person.name)
             listIds.add(tmp.userIdFirebase)

         }

         val array = arrayOfNulls<String>(listNames.size)
         listNames.toArray(array)
         println(Arrays.toString(array))


         val selectedList = ArrayList<Int>()
         val builder = AlertDialog.Builder(this)

         builder.setTitle("veuillez choisir les membres du groupe")
         builder.setMultiChoiceItems(
             array, null
         ) { dialog, which, isChecked ->
             if (isChecked) {
                 selectedList.add(which)
             } else if (selectedList.contains(which)) {
                 selectedList.remove(Integer.valueOf(which))
             }
         }

         builder.setPositiveButton("Valider") { dialogInterface, i ->
             //val selectedStrings = arrayListOf<String>()

             for (j in selectedList.indices) {
                 // array[selectedList[j]]?.let { selectedStrings.add(it) }
                 listeDesMembreDuGroupe.add(listIds.get(j))
             }

             Toast.makeText(
                 applicationContext,
                 "Les membres du groupe sont: " + Arrays.toString(listeDesMembreDuGroupe.toTypedArray()),
                 Toast.LENGTH_SHORT
             ).show()
         }

         builder.show()


     }*/

    private fun sendDataToFireBAse() {
        val progressdialog = indeterminateProgressDialog("Creation du groupe")
        FireStoreUtil.createGroupeChat(
            ParamModalFragment.listIdUserForGroup,
            nom_groupe.text.toString().trim(),
            descriptiongroupe.text.toString().trim(),
            onComplete = { idGroupe: String ->
                imageGroupeUri?.let { nonNulUri ->
                    StorageUtil.uploadImageOfGroupe(idGroupe, nonNulUri, onSuccess = { url: String ->
                        FireStoreUtil.updateImageGroup(url, idGroupe, onComplete = {
                            toast("groupe creer avec succes")
                            progressdialog.dismiss()

                            val myIntent = Intent(this, ChatGroupActivity::class.java)
                            myIntent.putExtra(AppConstants.ID_GROUPE, idGroupe)
                            myIntent.putExtra(AppConstants.USER_ID, FirebaseAuth.getInstance().currentUser?.uid)
                            myIntent.putExtra(AppConstants.NOM_GROUPE, nom_groupe.text.toString().trim())
                            //myIntent.putExtra(AppConstants.NOMBRE_MEMBRE_GROUPE, ParamModalFragment.listIdUserForGroup.size)
                            startActivity(myIntent)
                            finish()
                        })
                    })
                }
            })
    }


    private fun verifierChampOk(): Boolean {

        if (nom_groupe.text.toString().trim().equals("", true)) {
            nom_groupe.error = "Require!!"
            return false
        }
        return true
    }


    override fun onRestart() {
        super.onRestart()
        if (ParamModalFragment.listIdUserForGroup.size!= 0) {
            btn_creer_groupe.visibility = View.VISIBLE
        }
    }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED
            ) {
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE_GALERY);
            } else {
                //permission already granted
                startgallery()
            }
        } else {
            //system OS is < Marshmallow
            startgallery()
        }

    }

    fun startgallery() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(Intent.createChooser(intent, "Image de profil"), RC_SELECT_IMAGE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_CODE_GALERY -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    startgallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {

                if (resultCode == Activity.RESULT_OK) {

                    imageGroupeUri = data?.data

                    GlideApp.with(this)
                        .load(currentPhotoPath)
                        .transform(CircleCrop())
                        .into(imgAvatar)
                    //val imageBitmap = data?.extras?.get("data") as Bitmap
                    //imgAvatar.setImageBitmap(imageBitmap)
                }
            }

            RC_SELECT_IMAGE -> {

                imageGroupeUri = data?.data
                toast("URI: ${imageGroupeUri}")

                //val imageBitmap = data?.extras?.get("data") as Bitmap
                imgAvatar.setImageURI(imageGroupeUri)
                /* GlideApp.with(this)
                     .load(currentPhotoPath)
                     .transform(CircleCrop())
                     .into(imgAvatar)
                     */
            }
            else -> {
                Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onStop() {
        super.onStop()
        ParamModalFragment.listIdUserForGroup.clear()
    }

}
