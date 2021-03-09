package com.teamayka.vansaleandmgmt.ui.stockreport

import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.example.basil.zebraprinttest.BluetoothUtils
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import com.teamayka.vansaleandmgmt.utils.*
import com.teamayka.vansaleandmgmt.utils.PrintTask.Companion.FORMAT_STOCK_REPORT
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.util.*

class StockReportActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvRetry: TextView
    private lateinit var tvMessage: TextView
    private lateinit var fabEmail: FloatingActionButton
    private lateinit var fabPrint: FloatingActionButton
    private lateinit var fabSaveAsPDF: FloatingActionButton
    private lateinit var pbLoadingPrint: com.teamayka.vansaleandmgmt.components.ProgressBar

    private var callGetReport: Call? = null

    private var date = ""
    private var list1 = ArrayList<StockReportModel>()

//    private var callSaveAsPDF: Call? = null

//    private var ivId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_report)
        init()

        val groupId = intent.getStringExtra("KEY_GROUP_ID")
        val categoryId = intent.getStringExtra("KEY_CATEGORY_ID")
        val productId = intent.getStringExtra("KEY_PRODUCT_ID")
        date = intent.getStringExtra("KEY_DATE")
        rv.addItemDecoration(RvItemMargin(10))

        getReports(groupId, categoryId, productId, date)

        fabEmail.isEnabled = false
        fabPrint.isEnabled = false

        fabPrint.setOnClickListener {
            doPrint()
        }
//
//        fabSaveAsPDF.setOnClickListener {
//            if (TextUtils.isEmpty(ivId)) {
//                SnackBarUtils.showSnackBar(this, "No report to print")
//            } else
//                saveAsPDF()
//        }
    }

    private fun doPrint() {
        fabPrint.isEnabled = false
        pbLoadingPrint.visibility = View.VISIBLE
        bt.turnOn(this)
    }

/*    private fun saveAsPDF() {
        pbLoadingSaveAsPDF.visibility = View.VISIBLE
        fabSaveAsPDF.isEnabled = false

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("InvoiceID", ivId)

        val request = Request.Builder()
                .url(PublicUrls.URL_SAVE_AS_PDF)
                .post(body.build())
                .build()

        callSaveAsPDF = client.newCall(request)
        callSaveAsPDF?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("_____________error", " save as pdf: ${e?.message}")
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoadingSaveAsPDF.visibility = View.GONE
                    fabSaveAsPDF.isEnabled = true
                    if (e is UnknownHostException)
                        SnackBarUtils.showSnackBar(this@StockReportActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@StockReportActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp", "save as pdf$resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoadingSaveAsPDF.visibility = View.GONE
                            fabSaveAsPDF.isEnabled = true

                            SnackBarUtils.showSnackBar(this@StockReportActivity, message)
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoadingSaveAsPDF.visibility = View.GONE
                            fabSaveAsPDF.isEnabled = true
                            SnackBarUtils.showSnackBar(this@StockReportActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc", "save as pdf" + e.message)
                    runOnUiThread {
                        pbLoadingSaveAsPDF.visibility = View.GONE
                        fabSaveAsPDF.isEnabled = true
                        SnackBarUtils.showSnackBar(this@StockReportActivity, "$resp")
                    }
                }
            }
        })
    }*/

    private fun getReports(groupId: String, categoryId: String, productId: String, date: String) {
        tvMessage.visibility = View.GONE
        tvRetry.visibility = View.GONE
        fabEmail.isEnabled = false
        fabPrint.isEnabled = false
        pbLoading.visibility = View.VISIBLE

        tvRetry.setOnClickListener {
            getReports(groupId, categoryId, productId, date)
        }

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("LoginID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("GroupID", groupId)
                .add("CategoryID", categoryId)
                .add("ProductID", productId)
                .add("Date", date)
                .build()

        Log.e("__________post", "_stock_reports groupId : $groupId, categoryId : $categoryId, productId : $productId, date : $date")

        val url = "https://api.myjson.com/bins/p7jjw"
        val request = Request.Builder()
                .url(PublicUrls.URL_GET_STOCK_REPORT)
                .post(body)
                .build()

        callGetReport = client.newCall(request)
        callGetReport?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    fabEmail.isEnabled = false
                    fabPrint.isEnabled = false
                    tvMessage.visibility = View.VISIBLE
                    tvRetry.visibility = View.VISIBLE
                    pbLoading.visibility = View.GONE
                    if (e is UnknownHostException)
                        tvMessage.text = getString(R.string.error_message_connect_error)
                    else
                        tvMessage.text = getString(R.string.error_message_went_wront)
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp", "_stock_reports $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val jaStockReport = jo.getJSONArray("StockReport")
                        list1 = ArrayList()
                        for (i in 0 until jaStockReport.length()) {
                            val joStockReport = jaStockReport.getJSONObject(i)
                            val slNo = joStockReport.getString("Slno")
                            val barcode = joStockReport.getString("Barcode")
                            val item = joStockReport.getString("Item")
                            val group = joStockReport.getString("Group")
                            val category = joStockReport.getString("Category")
                            val stock = joStockReport.getString("Stock")
                            val unit = joStockReport.getString("Unit")
                            val wCP = joStockReport.getString("WCP")
                            val value = joStockReport.getString("Value")
                            list1.add(StockReportModel(slNo, group, item, barcode, unit, category, value, stock, wCP))
                        }

                        if (list1.size <= 0) {
                            runOnUiThread {
                                fabEmail.isEnabled = false
                                fabPrint.isEnabled = false
                                tvMessage.visibility = View.VISIBLE
                                tvRetry.visibility = View.VISIBLE
                                pbLoading.visibility = View.GONE

                                tvMessage.text = getString(R.string.empty_list)
                            }
                        } else {
                            runOnUiThread {
                                fabEmail.isEnabled = true
                                fabPrint.isEnabled = true
                                tvMessage.visibility = View.GONE
                                tvRetry.visibility = View.GONE
                                pbLoading.visibility = View.GONE

                                rv.adapter = StockReportRvAdapter(list1)
                            }
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            fabEmail.isEnabled = false
                            fabPrint.isEnabled = false
                            tvMessage.visibility = View.VISIBLE
                            tvRetry.visibility = View.VISIBLE
                            pbLoading.visibility = View.GONE
                            tvMessage.text = message
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_______exc", "_stock_reports" + e.message)
                    runOnUiThread {
                        fabEmail.isEnabled = false
                        fabPrint.isEnabled = false
                        tvMessage.visibility = View.VISIBLE
                        tvRetry.visibility = View.VISIBLE
                        pbLoading.visibility = View.GONE
                        tvMessage.text = "$resp"
                    }
                }
            }
        })
    }

    private val bt = BluetoothUtils(object : BluetoothUtils.BluetoothListener {
        override fun onSuccess(isOn: Boolean) {
            if (isOn) {
                dbp.discover(this@StockReportActivity)
            }
        }

        override fun onError() {
            fabPrint.isEnabled = true
            pbLoadingPrint.visibility = View.GONE
            SnackBarUtils.showSnackBar(this@StockReportActivity, "Cant turn on bluetooth")
        }
    })

    private val dbp = BluetoothPrinterDiscovery(object : BluetoothPrinterDiscovery.PrinterDiscoverListener {
        override fun onSuccess(discoveredPrinter: DiscoveredPrinter) {
            val connection = discoveredPrinter.connection
            try {
                connection.open()
                val zebraPrinter = ZebraPrinterFactory.getInstance(connection)

                if (zebraPrinter.currentStatus.isReadyToPrint || zebraPrinter.currentStatus.isPaused) {
                    val header = StringUtils.derialize(DataCollections.getInstance(this@StockReportActivity).getUser()!!.header)
                    val footer = StringUtils.derialize(DataCollections.getInstance(this@StockReportActivity).getUser()!!.footer)
                    printTask = PrintTask(printResult, connection, header, footer, StockReportDataModel(date,list1), zebraPrinter, FORMAT_STOCK_REPORT)
                    printTask!!.execute()
                } else {
                    val message = ZErr.getMessage(zebraPrinter.currentStatus)
                    SnackBarUtils.showSnackBar(this@StockReportActivity, message)
                    fabPrint.isEnabled = true
                    pbLoadingPrint.visibility = View.GONE
                }
            } catch (e: Exception) {
                fabPrint.isEnabled = true
                pbLoadingPrint.visibility = View.GONE
                SnackBarUtils.showSnackBar(this@StockReportActivity, "Can't connect to printer")
            }
        }

        override fun onFail() {
            fabPrint.isEnabled = true
            pbLoadingPrint.visibility = View.GONE
            SnackBarUtils.showSnackBar(this@StockReportActivity, "Printer not found")
        }
    })

    private val printResult = object : PrintTask.ZebraPrinterNotify {
        override fun notifyChange() {
            fabPrint.isEnabled = true
            pbLoadingPrint.visibility = View.GONE
            SnackBarUtils.showSnackBar(this@StockReportActivity, "Print success")
        }
    }

    private var printTask: PrintTask? = null

    override fun onBackPressed() {
        if (bt.isTurning() || dbp.isDiscovering() || (printTask != null && printTask!!.status == AsyncTask.Status.RUNNING)) {
            AlertDialog.Builder(this)
                    .setMessage("Do you want to cancel printing")
                    .setPositiveButton("YES") { p0, p1 ->
                        this@StockReportActivity.onBackPressed()
                        if (bt.isTurning())
                            bt.cancelListener(this@StockReportActivity)
                        if (dbp.isDiscovering())
                            dbp.cancelDiscovery()
                        if (printTask != null && printTask!!.status == AsyncTask.Status.RUNNING) {
                            printTask!!.cancel(true)
                        }
                    }
                    .setNegativeButton("CANCEL") { p0, p1 -> }
                    .show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callGetReport)
//        OkHttpUtils.cancelCalls(callSaveAsPDF)
    }

    private fun init() {
        rv = findViewById(R.id.rv)
        pbLoading = findViewById(R.id.pbLoading)
        tvRetry = findViewById(R.id.tvRetry)
        tvMessage = findViewById(R.id.tvMessage)
        fabEmail = findViewById(R.id.fabEmail)
        fabPrint = findViewById(R.id.fabPrint)
        fabSaveAsPDF = findViewById(R.id.fabSaveAsPDF)
        pbLoadingPrint = findViewById(R.id.pbLoadingPrint)
    }
}
