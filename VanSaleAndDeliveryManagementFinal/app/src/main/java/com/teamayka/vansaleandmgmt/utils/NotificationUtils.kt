package com.teamayka.vansaleandmgmt.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.teamayka.vansaleandmgmt.R


object NotificationUtils {
    fun showNotification(context: Context, title: String, message: String, autoCancel: Boolean, onGoing: Boolean, intent: PendingIntent) {
        val notifyId = 1
        val channelId = "single_channel"
        val channelName = context.getString(R.string.app_name)
        val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(onGoing)
                .setAutoCancel(autoCancel)
                .setContentIntent(intent)
                .setChannelId(channelId)
                .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(mChannel)
        }
        notificationManager.notify(notifyId, notification)
    }
}