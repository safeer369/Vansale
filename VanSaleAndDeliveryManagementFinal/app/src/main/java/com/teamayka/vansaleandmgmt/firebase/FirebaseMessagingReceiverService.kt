package com.teamayka.vansaleandmgmt.firebase

import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teamayka.vansaleandmgmt.utils.Constants


class FirebaseMessagingReceiverService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        Log.e("_____________", "onMessageReceived()")

        if (remoteMessage == null)
            return

        val data = remoteMessage.data
        Log.e("_______________", "data--- $data")

//        val handler = Handler(Looper.getMainLooper())
//        handler.post { Toast.makeText(applicationContext, "data:$data", Toast.LENGTH_SHORT).show() }

        val message = data["TicketID"]

        val intent = Intent(Constants.INTENT_FILTER_RECEIVE_NOTIFICATION)
        intent.putExtra("KEY_NOTIFICATION_MESSAGE", message)
        sendBroadcast(intent)

        // play default notification sound
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        val status = data["accept_status"]!!.toInt()
//        val message = data["message"].toString()
//
//        val user = DataCollections.getInstance(this).getUser()
//        if (user == null) {
//            val intent = Intent(this, CompanyLoginActivity::class.java)
//            val pendingIntent = PendingIntent.getActivity(this, 10, intent, 0)
//            if (status == -1) {
//                NotificationUtils.showNotification(this, "Accepted", message, true, true, pendingIntent)
//            } else {
//                NotificationUtils.showNotification(this, "Rejected", message, true, true, pendingIntent)
//            }
//        } else {
//            if (status == -1) {
//                val intent = Intent(this, CustomerPayNowActivity::class.java)
//                val pendingIntent = PendingIntent.getActivity(this, 10, intent, 0)
//                NotificationUtils.showNotification(this, "Accepted", message, true, true, pendingIntent)
//            } else {
//                val intent = Intent(this, RejectGasReadingActivity::class.java)
//                intent.putExtra("KEY_MESSAGE", message)
//                val pendingIntent = PendingIntent.getActivity(this, 10, intent, 0)
//                NotificationUtils.showNotification(this, "Rejected", message, true, true, pendingIntent)
//            }
//        }
    }
}