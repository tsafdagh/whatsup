package com.mirai.whatsup

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.facebook.common.util.UriUtil
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mirai.whatsup.glide.GlideApp
import com.mirai.whatsup.modal_fragments.ModalBottumFragment
import com.mirai.whatsup.modal_fragments.ParamModalFragment
import com.mirai.whatsup.utils.FireStoreUtil
import com.mirai.whatsup.utils.StorageUtil
import kotlinx.android.synthetic.main.activity_creation_groupe.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import java.io.ByteArrayOutputStream

class CreationGroupe : AppCompatActivity() {

    val CAMERA_REQUEST_CODE = 0
    val RC_SELECT_IMAGE = 5
    var imageGroupeUri: Uri? = null
    lateinit var imageFilePath: String

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
                        onclickImgAvatar()
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
                btn_creer_groupe.visibility = View.VISIBLE

                //TODO cette partir du code est code est à revoir
                if (ParamModalFragment.listIdUserForGroup.size != 0) {
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
            if(ParamModalFragment.listIdUserForGroup.size != 0 && imageGroupeUri !=null)
                sendDataToFireBAse()
            else{
                val snackbar = Snackbar.make(
                    id_vue_creer_groupe,
                    "Vous devez sélectionner des membres du groupe et/ou une image",
                    Snackbar.LENGTH_LONG
                )
                snackbar.show()
            }
        }
    }

    private fun sendDataToFireBAse() {
        val progressdialog = indeterminateProgressDialog("Creation du groupe")
        FireStoreUtil.createGroupeChat(ParamModalFragment.listIdUserForGroup,nom_groupe.text.toString().trim(),descriptiongroupe.text.toString().trim(), onComplete = {idGroupe:String ->
            imageGroupeUri?.let { nonNulUri ->
                StorageUtil.uploadImageOfGroupe(idGroupe, nonNulUri, onSuccess = {url:String ->
                    FireStoreUtil.updateImageGroup(url, idGroupe,onComplete = {
                        toast("groupe creer avec succes")
                        progressdialog.dismiss()
                    })
                })
            }
        })
    }


    private fun verifierChampOk(): Boolean {

        if (nom_groupe.text.toString().trim().equals("", true)) {
            nom_groupe.error = "Require!!"
            return  false
        }
        return true
    }


    override fun onRestart() {
        super.onRestart()
        if (ParamModalFragment.listIdUserForGroup.size != 0) {
            btn_creer_groupe.visibility = View.VISIBLE
        }
    }

    private fun openGallery() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(Intent.createChooser(intent, "Image de profil"), RC_SELECT_IMAGE)

    }

    private fun onclickImgAvatar() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    AlertDialog.Builder(this@CreationGroupe)
                        .setTitle("getString(R.string.storage_permission_rationale_title)")
                        .setMessage("getString(R.string.storage_permission_rationale_message)")
                        .setNegativeButton(
                            android.R.string.cancel
                        ) { dialogInterface, i ->
                            dialogInterface.dismiss()
                            token?.cancelPermissionRequest()
                        }
                        .setPositiveButton(android.R.string.ok) { dialogInterface, i ->
                            dialogInterface.dismiss()
                            token?.continuePermissionRequest()
                        }
                        .show()
                }

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted()!!) {

                        try {
                            val imageFile = createImageFile()
                            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            if (callCameraIntent.resolveActivity(packageManager) != null) {
                                val authorities = packageName + ".fileprovider"
                                val imageUri = FileProvider.getUriForFile(this@CreationGroupe, authorities, imageFile)
                                callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
                            }
                        } catch (e: IOException) {
                            Toast.makeText(this@CreationGroupe, "Could not create file!", Toast.LENGTH_SHORT).show()
                        }
                    }

                }

            }
            ).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        toast("dans le oncativity 0")

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                //toast("dans le oncativity 0.1")
                if (resultCode == Activity.RESULT_OK) {
                    //toast("dans le oncativity 1")

                    val imgUri = Uri.Builder()
                        .scheme(UriUtil.LOCAL_FILE_SCHEME)
                        .path(imageFilePath)
                        .build()
                    //toast("dans le oncativity 2")
                    imageGroupeUri = data?.data

                    GlideApp.with(this)
                        .load(imgUri?.path)
                        .transform(CircleCrop())
                        .into(imgAvatar)

                    //imgAvatar.setImageURI(imgUri, this)
                    //toast("second image uri is: ${imgUri}")
                    imageGroupeUri = imgUri
                }
            }

            RC_SELECT_IMAGE -> {
                var selectedImagePath = data?.data
                imageGroupeUri = data?.data
                toast("URI: ${selectedImagePath}")

                GlideApp.with(this)
                    .load(imageGroupeUri?.path)
                    .transform(CircleCrop())
                    .into(imgAvatar)


                //imgAvatar.setImageURI(selectedImagePath, this)
                /* selectedImagePath?.let {
                     StorageUtil.uploadFromLocalFile(it, onSuccess = {
                         toast("URL image: ${it}")
                         toast("User saved succesfully")
                         FireStoreUtil.initCurrentUserIfFirstTime(it, onComplete = {
                             progressdialog.dismiss()
                             startActivity(intentFor<MainActivity>().newTask().clearTask())
                         })
                     })
                 }*/
            }
            else -> {
                Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_SHORT).show()
            }
        }

    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName: String = "JPEG_" + timeStamp + "_"
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        imageFilePath = imageFile.absolutePath
        return imageFile
    }

}
