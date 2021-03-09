package com.teamayka.vansaleandmgmt.ui.main.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.utils.DataCollections
import com.teamayka.vansaleandmgmt.utils.OkHttpUtils
import com.teamayka.vansaleandmgmt.utils.SnackBarUtils
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class GetSuppliersActivity : AppCompatActivity() {

    private lateinit var layoutParent: View
    private lateinit var tvRetry: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvMessage: TextView

    private var callGetSuppliers: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_suppliers)
        init()

        if (DataCollections.getInstance(this).isTableEmpty(DataCollections.TABLE_NAME_SUPPLIERS)) {
            getSuppliers()
        } else {
            val intent = Intent(this@GetSuppliersActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        tvRetry.setOnClickListener {
            getSuppliers()
        }
    }

    private fun getSuppliers() {
        pbLoading.visibility = View.VISIBLE
        tvMessage.visibility = View.VISIBLE
        tvRetry.visibility = View.GONE

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .build()

        val url = "https://api.myjson.com/bins/qtevw"
        val request = Request.Builder()
                .url(PublicUrls.URL_GET_SUPPLIERS)
                .post(body)
                .build()

        callGetSuppliers = client.newCall(request)
        callGetSuppliers?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoading.visibility = View.GONE
                    tvMessage.visibility = View.GONE
                    tvRetry.visibility = View.VISIBLE
                    if (e is UnknownHostException)
                        SnackBarUtils.showSnackBar(this@GetSuppliersActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@GetSuppliersActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("____________resp", "_get_suppliers : $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {

                        val jaData = jo.getJSONArray("Data")
                        for (i in 0 until jaData.length()) {
                            val joData = jaData.getJSONObject(i)
                            val supplierId = joData.getString("ID")
                            val code = joData.getString("Code")
                            val name = joData.getString("Name")
                            val address1 = joData.getString("Address1")
                            val city = joData.getString("City")
                            val telephone = joData.getString("Telephone")
                            val ratePlan = joData.getString("RatePlan")
                            DataCollections.getInstance(this@GetSuppliersActivity).addSupplier(
                                    supplierId,
                                    code,
                                    name,
                                    address1,
                                    city,
                                    telephone,
                                    ratePlan
                            )
                        }

                        val intent = Intent(this@GetSuppliersActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoading.visibility = View.GONE
                            tvMessage.visibility = View.GONE
                            tvRetry.visibility = View.VISIBLE
                            SnackBarUtils.showSnackBar(this@GetSuppliersActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_________exc_suppliers", e.message)
                    runOnUiThread {
                        pbLoading.visibility = View.GONE
                        tvMessage.visibility = View.GONE
                        tvRetry.visibility = View.VISIBLE
                        SnackBarUtils.showSnackBar(this@GetSuppliersActivity, "$resp")
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callGetSuppliers)
    }

    override fun onBackPressed() {
        if (pbLoading.visibility == View.VISIBLE) {
            AlertDialog.Builder(this)
                    .setMessage("Adding Suppliers... Please wait...")
                    .setPositiveButton("STOP") { p0, p1 ->
                        OkHttpUtils.cancelCalls(callGetSuppliers)
                        finish()
                        DataCollections.getInstance(this@GetSuppliersActivity).clearSuppliers()
                    }
                    .setNegativeButton("CANCEL") { p0, p1 -> }
                    .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun init() {
        tvRetry = findViewById(R.id.tvRetry)
        layoutParent = findViewById(R.id.layoutParent)
        pbLoading = findViewById(R.id.pbLoading)
        tvMessage = findViewById(R.id.tvMessage)
    }
}
