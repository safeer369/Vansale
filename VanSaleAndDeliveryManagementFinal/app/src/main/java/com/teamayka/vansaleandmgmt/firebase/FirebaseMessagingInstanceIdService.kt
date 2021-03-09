package com.teamayka.vansaleandmgmt.firebase

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.teamayka.vansaleandmgmt.utils.DataCollections
import com.teamayka.vansaleandmgmt.utils.OkHttpUtils
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class FirebaseMessagingInstanceIdService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        super.onTokenRefresh()
        Log.e("________", "____________onTokenRefresh()")
        val user = DataCollections.getInstance(this).getUser()?.id ?: return

        val fcm = FirebaseInstanceId.getInstance().token ?: return
        DataCollections.getInstance(this).updateFcmSyncStatus(fcm, false)
        updateFCM(user, fcm)
    }

    private fun updateFCM(user: String, fcm: String) {
        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", user)
                .add("FCMID", fcm)
                .build()

        val request = Request.Builder()
                .url(PublicUrls.URL_UPDATE_FCM_ID)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                Log.e("___________fail", "_update_fcm: ${e?.message}")
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_______resp_update_fcm", resp)
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        DataCollections.getInstance(this@FirebaseMessagingInstanceIdService).updateFcmSyncStatus(fcm, true)
                    }
                } catch (e: Exception) {
                    Log.e("________ex", "update fcm :" + e.message)
                }
            }
        })
    }
}