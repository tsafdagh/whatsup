package com.mirai.whatsup.application

import android.app.Application
import android.content.Context
import com.facebook.drawee.backends.pipeline.Fresco
import android.support.multidex.MultiDex


class WhatSupApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        try {
            MultiDex.install(this)
        } catch (multiDexException: RuntimeException) {
            multiDexException.printStackTrace()
        }

    }
}