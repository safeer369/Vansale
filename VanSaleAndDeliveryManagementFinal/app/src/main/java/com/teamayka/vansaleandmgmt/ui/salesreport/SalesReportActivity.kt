package com.teamayka.vansaleandmgmt.ui.salesreport

import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import com.example.basil.zebraprinttest.BluetoothUtils
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import com.teamayka.vansaleandmgmt.ui.main.adapters.SrAdapter
import com.teamayka.vansaleandmgmt.ui.main.models.CustomerModel
import com.teamayka.vansaleandmgmt.utils.*
import com.teamayka.vansaleandmgmt.utils.PrintTask.Companion.FORMAT_SALES_REPORT
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SalesReportActivity : AppCompatActivity() {

    private lateinit var sCustomers: Spinner
    private lateinit var rv: RecyclerView
    private lateinit var tvDateFrom: TextView
    private lateinit var tvDateTo: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvValueGrossAmount: TextView
    private lateinit var tvValueDiscountAmount: TextView
    private lateinit var tvValueNetAmount: TextView
    private lateinit var fabSearch: FloatingActionButton
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvRetry: TextView
    private lateinit var tvMessage: TextView
    private lateinit var fabEmail: FloatingActionButton
    private lateinit var fabPrint: FloatingActionButton
    private lateinit var fabSaveAsPDF: FloatingActionButton
    private lateinit var pbLoadingSaveAsPDF: com.teamayka.vansaleandmgmt.components.ProgressBar
    private lateinit var pbLoadingEmail: com.teamayka.vansaleandmgmt.components.ProgressBar
    private lateinit var pbLoadingPrint: com.teamayka.vansaleandmgmt.components.ProgressBar

    private var callGetReport: Call? = null
    private var callSaveAsPDF: Call? = null
    private var callEmail: Call? = null

    private var listSalesReport = ArrayList<SalesReportModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_report)
        init()

        tvValueGrossAmount.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)
        tvValueDiscountAmount.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)
        tvValueNetAmount.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)

        rv.addItemDecoration(RvItemMargin(10))

        val currentDate = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        tvDateFrom.text = df.format(currentDate)
        tvDateTo.text = df.format(currentDate)

        tvDateFrom.setOnClickListener {
            val cal = Calendar.getInstance()
            val pickerDialog = DatePickerDialog.newInstance({ view, year, monthOfYear, dayOfMonth ->
                val date = "$year-${monthOfYear + 1}-$dayOfMonth"
                tvDateFrom.text = date
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            pickerDialog.accentColor = ResourcesCompat.getColor(resources, R.color.colorBgDefaultBlue, null)
            pickerDialog.show(fragmentManager, "DatePickerDialog")

        }

        tvDateTo.setOnClickListener {
            val cal = Calendar.getInstance()
            val pickerDialog = DatePickerDialog.newInstance({ view, year, monthOfYear, dayOfMonth ->
                val date = "$year-${monthOfYear + 1}-$dayOfMonth"
                tvDateTo.text = date
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            pickerDialog.accentColor = ResourcesCompat.getColor(resources, R.color.colorBgDefaultBlue, null)
            pickerDialog.show(fragmentManager, "DatePickerDialog")
        }

        val list = DataCollections.getInstance(this).getCustomers()
        list.add(0, CustomerModel("-1", "", "---", "", "", "", ""))
        sCustomers.adapter = SrAdapter(this, list)
        fabSearch.setOnClickListener {
            val customerId = list[sCustomers.selectedItemPosition].customerId
            val dateFrom = tvDateFrom.text.toString().trim()
            val dateTo = tvDateTo.text.toString().trim()
            getReports(customerId, dateFrom, dateTo)
        }
        fabEmail.isEnabled = false
        fabPrint.isEnabled = false
        fabSaveAsPDF.isEnabled = false

        fabEmail.setOnClickListener {
            val customerId = list[sCustomers.selectedItemPosition].customerId
            val dateFrom = tvDateFrom.text.toString().trim()
            val dateTo = tvDateTo.text.toString().trim()
            doEmail(customerId, dateFrom, dateTo)
        }

        fabPrint.setOnClickListener {
            doPrint()
        }

        fabSaveAsPDF.setOnClickListener {
            val customerId = list[sCustomers.selectedItemPosition].customerId
            val dateFrom = tvDateFrom.text.toString().trim()
            val dateTo = tvDateTo.text.toString().trim()
//            saveAsPDF(customerId, dateFrom, dateTo)
        }
    }

    private fun doEmail(customerId: String, dateFrom: String, dateTo: String) {
        pbLoadingEmail.visibility = View.VISIBLE
        fabEmail.isEnabled = false

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("datefrom", dateFrom)
                .add("dateTo", dateTo)
                .add("customerID", customerId)

        val request = Request.Builder()
                .url(PublicUrls.URL_EMAIL)
                .post(body.build())
                .build()

        callEmail = client.newCall(request)
        callEmail?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("_____________error", " email: ${e?.message}")
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoadingEmail.visibility = View.GONE
                    fabEmail.isEnabled = true
                    if (e is UnknownHostException)
                        SnackBarUtils.showSnackBar(this@SalesReportActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@SalesReportActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp", "email $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoadingEmail.visibility = View.GONE
                            fabEmail.isEnabled = true

                            SnackBarUtils.showSnackBar(this@SalesReportActivity, message)
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoadingEmail.visibility = View.GONE
                            fabEmail.isEnabled = true
                            SnackBarUtils.showSnackBar(this@SalesReportActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc", "email" + e.message)
                    runOnUiThread {
                        pbLoadingEmail.visibility = View.GONE
                        fabEmail.isEnabled = true
                        SnackBarUtils.showSnackBar(this@SalesReportActivity, "$resp")
                    }
                }
            }
        })
    }

    private fun doPrint() {
        fabPrint.isEnabled = false
        pbLoadingPrint.visibility = View.VISIBLE
        bt.turnOn(this)
    }

//    private fun saveAsPDF(customerId: String, dateFrom: String, dateTo: String) {
//        pbLoadingSaveAsPDF.visibility = View.VISIBLE
//        fabSaveAsPDF.isEnabled = false
//
//        val client = OkHttpUtils.getOkHttpClient()
//
//        val body = FormBody.Builder()
//                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
//                .add("datefrom", dateFrom)
//                .add("dateTo", dateTo)
//                .add("customerID", customerId)
//
//        val request = Request.Builder()
//                .url(PublicUrls.URL_SAVE_AS_PDF)
//                .post(body.build())
//                .build()
//
//        callSaveAsPDF = client.newCall(request)
//        callSaveAsPDF?.enqueue(object : Callback {
//            override fun onFailure(call: Call?, e: IOException?) {
//                Log.e("_____________error", " save as pdf: ${e?.message}")
//                if (call == null || call.isCanceled)
//                    return
//                runOnUiThread {
//                    pbLoadingSaveAsPDF.visibility = View.GONE
//                    fabSaveAsPDF.isEnabled = true
//                    if (e is UnknownHostException)
//                        SnackBarUtils.showSnackBar(this@SalesReportActivity, getString(R.string.error_message_connect_error))
//                    else
//                        SnackBarUtils.showSnackBar(this@SalesReportActivity, getString(R.string.error_message_went_wront))
//                }
//            }
//
//            override fun onResponse(call: Call?, response: Response?) {
//                if (call == null || call.isCanceled)
//                    return
//                val resp = response?.body()?.string()
//                Log.e("_____________resp", "save as pdf$resp")
//                try {
//                    val jo = JSONObject(resp)
//                    val result = jo.getBoolean("Result")
//                    if (result) {
//                        val message = jo.getString("Message")
//                        runOnUiThread {
//                            pbLoadingSaveAsPDF.visibility = View.GONE
//                            fabSaveAsPDF.isEnabled = true
//
//                            SnackBarUtils.showSnackBar(this@SalesReportActivity, message)
//                        }
//                    } else {
//                        val message = jo.getString("Message")
//                        runOnUiThread {
//                            pbLoadingSaveAsPDF.visibility = View.GONE
//                            fabSaveAsPDF.isEnabled = true
//                            SnackBarUtils.showSnackBar(this@SalesReportActivity, message)
//                        }
//                    }
//                } catch (e: Exception) {
//                    Log.e("_____________exc", "save as pdf" + e.message)
//                    runOnUiThread {
//                        pbLoadingSaveAsPDF.visibility = View.GONE
//                        fabSaveAsPDF.isEnabled = true
//                        SnackBarUtils.showSnackBar(this@SalesReportActivity, "$resp")
//                    }
//                }
//            }
//        })
//    }

    private fun getReports(customerId: String, dateFrom: String, dateTo: String) {
        tvDateFrom.isEnabled = false
        tvDateTo.isEnabled = false
        sCustomers.isEnabled = false
        fabSearch.isEnabled = false
        tvMessage.visibility = View.GONE
        tvRetry.visibility = View.GONE
        tvUsername.visibility = View.GONE
        tvValueGrossAmount.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)
        tvValueDiscountAmount.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)
        tvValueNetAmount.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)
        fabEmail.isEnabled = false
        fabPrint.isEnabled = false
        pbLoading.visibility = View.VISIBLE

        tvRetry.setOnClickListener {
            getReports(customerId, dateFrom, dateTo)
        }

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("LoginID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("CustomerID", customerId)
                .add("dtFrom", dateFrom)
                .add("dtTo", dateTo)
                .build()

        val url = "https://api.myjson.com/bins/p7jjw"
        val request = Request.Builder()
                .url(PublicUrls.URL_GET_SALES_REPORT)
                .post(body)
                .build()

        callGetReport = client.newCall(request)
        callGetReport?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    tvDateFrom.isEnabled = true
                    tvDateTo.isEnabled = true
                    sCustomers.isEnabled = true
                    fabSearch.isEnabled = true
                    fabEmail.isEnabled = false
                    fabPrint.isEnabled = false
                    tvUsername.visibility = View.GONE
                    tvValueGrossAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
                    tvValueDiscountAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
                    tvValueNetAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
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
                Log.e("_____________resp", "_sales_reports $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val joData = jo.getJSONObject("Data")
                        val userName = joData.getString("UserName")
                        val totalGrossAmount = joData.getString("GrossAmount")
                        val totalDiscountAmount = joData.getString("DiscountAmount")
                        val totalNetAmount = joData.getString("NetAmount")
                        val jaSalesReport = jo.getJSONArray("SalesReport")
                        listSalesReport = ArrayList()
                        for (i in 0 until jaSalesReport.length()) {
                            val joSalesReport = jaSalesReport.getJSONObject(i)
                            val slNo = joSalesReport.getString("Slno")
                            val invoiceNo = joSalesReport.getString("InvoiceNo")
                            val invoiceDate = joSalesReport.getString("InvoiceDate")
                            val customerName = joSalesReport.getString("CustomerName")
                            val grossAmount = joSalesReport.getString("GrossAmount")
                            val discount = joSalesReport.getString("Discount")
                            val netAmount = joSalesReport.getString("NetAmount")
                            listSalesReport.add(SalesReportModel(
                                    slNo,
                                    invoiceNo,
                                    invoiceDate,
                                    customerName,
                                    grossAmount,
                                    discount,
                                    netAmount
                            ))
                        }

                        runOnUiThread {
                            tvDateFrom.isEnabled = true
                            tvDateTo.isEnabled = true
                            sCustomers.isEnabled = true
                            fabSearch.isEnabled = true
                            pbLoading.visibility = View.GONE

                            if (listSalesReport.size <= 0) {
                                fabEmail.isEnabled = false
                                fabPrint.isEnabled = false
                                tvMessage.visibility = View.VISIBLE
                                tvRetry.visibility = View.VISIBLE
                                tvMessage.text = getString(R.string.empty_list)

                                rv.adapter = SalesReportRvAdapter(ArrayList()) // empty recycler view

                                tvUsername.visibility = View.GONE
                                tvValueDiscountAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
                                tvValueGrossAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
                                tvValueNetAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
                            } else {
                                fabEmail.isEnabled = true
                                fabPrint.isEnabled = true
                                fabSaveAsPDF.isEnabled = true
                                tvMessage.visibility = View.GONE
                                tvRetry.visibility = View.GONE
                                tvUsername.visibility = View.VISIBLE
                                tvUsername.text = userName
                                tvValueDiscountAmount.text = totalDiscountAmount
                                tvValueGrossAmount.text = totalGrossAmount
                                tvValueNetAmount.text = totalNetAmount

                                rv.adapter = SalesReportRvAdapter(listSalesReport)
                            }
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            tvDateFrom.isEnabled = true
                            tvDateTo.isEnabled = true
                            sCustomers.isEnabled = true
                            fabSearch.isEnabled = true
                            fabEmail.isEnabled = false
                            fabPrint.isEnabled = false
                            tvUsername.visibility = View.GONE
                            tvValueGrossAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
                            tvValueDiscountAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
                            tvValueNetAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
                            tvMessage.visibility = View.VISIBLE
                            tvRetry.visibility = View.VISIBLE
                            pbLoading.visibility = View.GONE
                            tvMessage.text = message
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_______exc", "_sales_reports" + e.message)
                    runOnUiThread {
                        tvDateFrom.isEnabled = true
                        tvDateTo.isEnabled = true
                        sCustomers.isEnabled = true
                        fabSearch.isEnabled = true
                        fabEmail.isEnabled = false
                        fabPrint.isEnabled = false
                        tvUsername.visibility = View.GONE
                        tvValueGrossAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
                        tvValueDiscountAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
                        tvValueNetAmount.text = ("%." + DataCollections.getInstance(this@SalesReportActivity).getUser()!!.amountType + "f").format(0.0)
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
                dbp.discover(this@SalesReportActivity)
            }
        }

        override fun onError() {
            fabPrint.isEnabled = true
            pbLoadingPrint.visibility = View.GONE
            SnackBarUtils.showSnackBar(this@SalesReportActivity, "Cant turn on bluetooth")
        }
    })

    private val dbp = BluetoothPrinterDiscovery(object : BluetoothPrinterDiscovery.PrinterDiscoverListener {
        override fun onSuccess(discoveredPrinter: DiscoveredPrinter) {
            val connection = discoveredPrinter.connection
            try {
                connection.open()
                val zebraPrinter = ZebraPrinterFactory.getInstance(connection)

                if (zebraPrinter.currentStatus.isReadyToPrint || zebraPrinter.currentStatus.isPaused) {
                    val header = StringUtils.derialize(DataCollections.getInstance(this@SalesReportActivity).getUser()!!.header)
                    val footer = StringUtils.derialize(DataCollections.getInstance(this@SalesReportActivity).getUser()!!.footer)
                    val date = CalendarUtils.getCurrentDate()
                    val discountAmt = tvValueDiscountAmount.text.toString()
                    val grossAmt = tvValueGrossAmount.text.toString()
                    val netAmt = tvValueNetAmount.text.toString()
                    val username = tvUsername.text.toString()

                    printTask = PrintTask(printResult, connection, header, footer, SalesReportDataModel(date, discountAmt, grossAmt, netAmt, username, listSalesReport), zebraPrinter, FORMAT_SALES_REPORT)
                    printTask!!.execute()
                } else {
                    val message = ZErr.getMessage(zebraPrinter.currentStatus)
                    SnackBarUtils.showSnackBar(this@SalesReportActivity, message)
                    fabPrint.isEnabled = true
                    pbLoadingPrint.visibility = View.GONE
                }
            } catch (e: Exception) {
                fabPrint.isEnabled = true
                pbLoadingPrint.visibility = View.GONE
                SnackBarUtils.showSnackBar(this@SalesReportActivity, "Can't connect to printer")
            }
        }

        override fun onFail() {
            fabPrint.isEnabled = true
            pbLoadingPrint.visibility = View.GONE
            SnackBarUtils.showSnackBar(this@SalesReportActivity, "Printer not found")
        }
    })

    private val printResult = object : PrintTask.ZebraPrinterNotify {
        override fun notifyChange() {
            fabPrint.isEnabled = true
            pbLoadingPrint.visibility = View.GONE
            SnackBarUtils.showSnackBar(this@SalesReportActivity, "Print success")
        }
    }

    private var printTask: PrintTask? = null

    override fun onBackPressed() {
        if (bt.isTurning() || dbp.isDiscovering() || (printTask != null && printTask!!.status == AsyncTask.Status.RUNNING)) {
            AlertDialog.Builder(this)
                    .setMessage("Do you want to cancel printing")
                    .setPositiveButton("YES") { p0, p1 ->
                        this@SalesReportActivity.onBackPressed()
                        if (bt.isTurning())
                            bt.cancelListener(this@SalesReportActivity)
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
        OkHttpUtils.cancelCalls(callSaveAsPDF)
        OkHttpUtils.cancelCalls(callEmail)
    }

    private fun init() {
        sCustomers = findViewById(R.id.sCustomers)
        rv = findViewById(R.id.rv)
        tvDateFrom = findViewById(R.id.tvDateFrom)
        tvDateTo = findViewById(R.id.tvDateTo)
        tvUsername = findViewById(R.id.tvUsername)
        tvValueGrossAmount = findViewById(R.id.tvValueGrossAmount)
        tvValueDiscountAmount = findViewById(R.id.tvValueDiscountAmount)
        tvValueNetAmount = findViewById(R.id.tvValueNetAmount)
        fabSearch = findViewById(R.id.fabSearch)
        pbLoading = findViewById(R.id.pbLoading)
        tvRetry = findViewById(R.id.tvRetry)
        tvMessage = findViewById(R.id.tvMessage)
        fabEmail = findViewById(R.id.fabEmail)
        fabPrint = findViewById(R.id.fabPrint)
        fabSaveAsPDF = findViewById(R.id.fabSaveAsPDF)
        pbLoadingSaveAsPDF = findViewById(R.id.pbLoadingSaveAsPDF)
        pbLoadingEmail = findViewById(R.id.pbLoadingEmail)
        pbLoadingPrint = findViewById(R.id.pbLoadingPrint)
    }
}
