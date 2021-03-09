package com.teamayka.vansaleandmgmt.ui.main.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.teamayka.vansaleandmgmt.utils.OkHttpUtils
import com.teamayka.vansaleandmgmt.utils.DataCollections
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.utils.SnackBarUtils
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class GetProductsActivity : AppCompatActivity() {

    private lateinit var layoutParent: View
    private lateinit var tvRetry: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvMessage: TextView

    private var callGetProducts: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_products)
        init()

        if (DataCollections.getInstance(this).isTableEmpty(DataCollections.TABLE_NAME_PRODUCTS)) {
            getProducts()
        } else {
            val intent = Intent(this@GetProductsActivity, GetSuppliersActivity::class.java)
            startActivity(intent)
            finish()
        }

        tvRetry.setOnClickListener {
            getProducts()
        }
    }

    private fun getProducts() {
        pbLoading.visibility = View.VISIBLE
        tvMessage.visibility = View.VISIBLE
        tvRetry.visibility = View.GONE

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .build()

        val url = "https://api.myjson.com/bins/p7jjw"
        val request = Request.Builder()
                .url(PublicUrls.URL_GET_PRODUCTS)
                .post(body)
                .build()

        callGetProducts = client.newCall(request)
        callGetProducts?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoading.visibility = View.GONE
                    tvMessage.visibility = View.GONE
                    tvRetry.visibility = View.VISIBLE
                    if (e is UnknownHostException)
                        SnackBarUtils.showSnackBar(this@GetProductsActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@GetProductsActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp", "_get_products : $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {

                        val jaData = jo.getJSONArray("Data")
                        for (i in 0 until jaData.length()) {
                            val joData = jaData.getJSONObject(i)
                            val productId = joData.getString("ProductID")
                            val productCode = joData.getString("ProductCode")
                            val productName = joData.getString("ProductName")
                            val displayName = joData.getString("DisplayName")
                            val unit = joData.getString("Unit")
                            val unitId = joData.getString("UnitID")
                            val cGST = joData.getString("CGST")
                            val sGST = joData.getString("SGST")
                            val rate1 = joData.getString("Rate1")
                            val rate2 = joData.getString("Rate2")
                            val rate3 = joData.getString("Rate3")
                            val taxInclusive = joData.getString("TaxInclusive")
                            val imageUrl = joData.getString("ImageUrl")
                            DataCollections.getInstance(this@GetProductsActivity).addProduct(
                                    productId,
                                    productCode,
                                    productName,
                                    displayName,
                                    unit,
                                    unitId,
                                    cGST,
                                    sGST,
                                    rate1,
                                    rate2,
                                    rate3,
                                    taxInclusive,
                                    imageUrl
                            )
                        }
                        // todo remove this line. it is for barcode product testing
                        DataCollections.getInstance(this@GetProductsActivity).addProduct(
                                "50",
                                "89007655",
                                "JAM",
                                "JAM",
                                "BOX",
                                "1",
                                "0",
                                "0",
                                "100",
                                "203",
                                "120",
                                "true",
                                "http://www.google.com"
                        )
                        val intent = Intent(this@GetProductsActivity, GetSuppliersActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoading.visibility = View.GONE
                            tvMessage.visibility = View.GONE
                            tvRetry.visibility = View.VISIBLE
                            SnackBarUtils.showSnackBar(this@GetProductsActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc_login", e.message)
                    runOnUiThread {
                        pbLoading.visibility = View.GONE
                        tvMessage.visibility = View.GONE
                        tvRetry.visibility = View.VISIBLE
                        SnackBarUtils.showSnackBar(this@GetProductsActivity, "$resp")
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callGetProducts)
    }

    override fun onBackPressed() {
        if (pbLoading.visibility == View.VISIBLE) {
            AlertDialog.Builder(this)
                    .setMessage("Adding Products... Please wait...")
                    .setPositiveButton("STOP") { p0, p1 ->
                        OkHttpUtils.cancelCalls(callGetProducts)
                        finish()
                        DataCollections.getInstance(this@GetProductsActivity).clearProducts()
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
