package com.teamayka.vansaleandmgmt.utils

import okhttp3.Call
import okhttp3.OkHttpClient

/**
 * Created by Administrator on 10/3/2017.
 */

object OkHttpUtils {

    private var client: OkHttpClient? = null

    fun getOkHttpClient(): OkHttpClient {
        if (client == null)
            client = OkHttpClient()
        return client!!
    }

    fun cancelCalls(vararg calls: Call?) {
        for (call in calls) {
            cancelCall(call)
        }
    }

    private fun cancelCall(call: Call?) {
        call?.cancel()
    }
}
