package com.teamayka.vansaleandmgmt.utils

import android.app.Application
import com.teamayka.vansaleandmgmt.devtools.UnCaughtException

/**
 * Created by Administrator on 10/27/2017.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(UnCaughtException(applicationContext))
    }
}