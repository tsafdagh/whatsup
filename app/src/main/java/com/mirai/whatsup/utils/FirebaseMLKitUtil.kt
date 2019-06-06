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
}