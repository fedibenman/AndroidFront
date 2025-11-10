package com.example.myapplication

import android.app.Application
import android.content.Context

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextHolder.init(this)
    }
}

object AppContextHolder {
    private lateinit var appContextInternal: Context

    fun init(context: Context) {
        appContextInternal = context.applicationContext
    }

    val appContext: Context
        get() = appContextInternal
}
