package com.teamayka.vansaleandmgmt.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.ImageView
import com.teamayka.vansaleandmgmt.utils.Constants

class FireBaseNotificationBroadcastReceiver(private val ivNotification: ImageView) : BroadcastReceiver() {
    override fun onReceive(p0: Context?, intent: Intent?) {
        if (intent != null && intent.action == Constants.INTENT_FILTER_RECEIVE_NOTIFICATION) {
            val message = intent.getStringExtra("KEY_NOTIFICATION_MESSAGE")
            ivNotification.visibility = View.VISIBLE
            ivNotification.setOnClickListener {
                AlertDialog.Builder(ivNotification.context)
                        .setMessage(message)
                        .setPositiveButton("OK") { p0, p1 -> }
                        .show()
            }
        }
    }
}