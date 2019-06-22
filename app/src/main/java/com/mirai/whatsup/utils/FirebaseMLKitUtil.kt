package com.mirai.whatsup.utils

import android.content.Context
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.mirai.whatsup.AppConstants
import org.jetbrains.anko.indeterminateProgressDialog

object FirebaseMlKitUtil {

    fun translateToEnglish(stringMsg: String, onComplete: (translatedMessage: String) -> Unit) {

        // Create an French-English translator:
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.FR)
            .setTargetLanguage(FirebaseTranslateLanguage.EN)
            .build()
        val frenchEnglishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options)

        frenchEnglishTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                frenchEnglishTranslator.translate(stringMsg)
                    .addOnSuccessListener { translatedText ->
                        onComplete(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        onComplete("-1")

                    }
            }
            .addOnFailureListener { exception ->
                onComplete("-1")
            }
    }


    /*
    * cette fonction permet de traduireun texte donnée en
    * n'importe quelle langue
    * */
    fun translateToAnyLanguage(
        text: String,
        sourcelanguage: Int,
        targetlanguage: Int,
        onComplete: (translatedMessage: String) -> Unit
    ) {
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(sourcelanguage)
            .setTargetLanguage(targetlanguage)
            .build()
        val frenchEnglishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options)

        frenchEnglishTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                frenchEnglishTranslator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        onComplete(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        onComplete("-1")

                    }
            }
            .addOnFailureListener { exception ->
                onComplete("-1")
            }
    }


    fun translateMsg(language: String, textToTranslate: String, onComplete: (translatedMessage: String) -> Unit) {

        var srcCodeLanguage = -1
        var destCodeLanguage = -1
        if (language.equals(AppConstants.ENGLISH)) {
            srcCodeLanguage = FirebaseTranslateLanguage.FR
            destCodeLanguage = FirebaseTranslateLanguage.EN
        } else if (language.equals(AppConstants.FRENCH)) {
            srcCodeLanguage = FirebaseTranslateLanguage.EN
            destCodeLanguage = FirebaseTranslateLanguage.FR
        }

        if (srcCodeLanguage == -1 || destCodeLanguage == -1) {
            onComplete(textToTranslate)
        } else {
            val options = FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(srcCodeLanguage)
                .setTargetLanguage(destCodeLanguage)
                .build()
            val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    translator.translate(textToTranslate)
                        .addOnSuccessListener { translatedText ->
                            onComplete(translatedText)
                        }
                        .addOnFailureListener { exception ->
                            onComplete(textToTranslate)

                        }
                }
                .addOnFailureListener { exception ->
                    onComplete(textToTranslate)
                }
        }
    }

    fun downloadEhglishFrenchLanguage(context: Context) {
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.FR)
            .setTargetLanguage(FirebaseTranslateLanguage.EN)
            .build()
        val progressdialog = context.indeterminateProgressDialog("Telechargement des langues...")
        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                progressdialog.dismiss()
            }
            .addOnFailureListener { exception ->
                progressdialog.dismiss()
            }
    }
}