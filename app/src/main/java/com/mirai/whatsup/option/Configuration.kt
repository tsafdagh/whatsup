package com.mirai.whatsup.option

import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage

class Configuration {

    companion object {
        var istranslateMessaActived = false
        var translete_language = FirebaseTranslateLanguage.EN
        var oldLanguage = FirebaseTranslateLanguage.EN
    }
}