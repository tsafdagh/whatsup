package com.mirai.whatsup.utils

import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions

object FirebaseMlKitUtil{

    fun translateToEnglish(stringMsg: String, onComplete: (translatedMessage:String) -> Unit){

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
    fun translateToAnyLanguage(text:String, sourcelanguage: Int, targetlanguage: Int, onComplete: (translatedMessage: String) -> Unit){
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
}