package com.teamayka.vansaleandmgmt.utils

import android.app.Activity
import android.support.design.widget.Snackbar
import android.view.View


/**
 * Created by seydbasil on 3/27/2018.
 */
object SnackBarUtils {
    fun showSnackBar(activity: Activity, message: String) {
        val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }
}