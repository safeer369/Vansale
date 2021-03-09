package com.teamayka.vansaleandmgmt.ui.salesreturn.activities

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.basil.zebraprinttest.BluetoothUtils
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import com.teamayka.vansaleandmgmt.ui.main.models.ProductModel
import com.teamayka.vansaleandmgmt.ui.sales.models.SalesDataModel
import com.teamayka.vansaleandmgmt.utils.*
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.util.*


class SalesReturnFinishActivity : AppCompatActivity() {

    private lateinit var layoutParent: View
    private lateinit var fabSave: FloatingActionButton
    private lateinit var fabSaveAsPDF: FloatingActionButton
    private lateinit var fabPrint: FloatingActionButton
    private lateinit var fabCancel: FloatingActionButton
    private lateinit var pbLoadingSave: ImageView
    private lateinit var pbLoadingSaveAsPDF: ImageView
    private lateinit var pbLoadingPrint: ImageView
    private lateinit var tvValueTotal: TextView
    private lateinit var tvValueDiscountAmount: TextView
    private lateinit var tvValueCash: TextView
    private lateinit var tvValueCredit: TextView
    private lateinit var tvValueBalance: TextView

    private var callSaveInvoice: Call? = null
    private var callSaveAsPDF: Call? = null

    private var ivId = ""
    private var ivNo = ""
    private var date = ""
    private var name = ""
    private var total = ""
    private var taxAmount = ""
    private var netTotal = ""
    private var list = ArrayList<ProductModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_return_finish)
        init()

        val isUpdate = intent.getBooleanExtra("KEY_IS_UPDATE", false)
        val invoiceId = if (isUpdate) intent.getStringExtra("KEY_INVOICE_ID") else ""
        list = intent.extras.getParcelableArrayList<ProductModel>("KEY_LIST")
        date = intent.getStringExtra("KEY_DATE")
        val customerId = intent.getStringExtra("KEY_CUSTOMER_ID")
        name = intent.getStringExtra("KEY_NAME")
        val address = intent.getStringExtra("KEY_ADDRESS")
        val phone = intent.getStringExtra("KEY_PHONE")
        total = intent.getStringExtra("KEY_TOTAL")
        taxAmount = intent.getStringExtra("KEY_TAX_AMOUNT")
        val discount = intent.getStringExtra("KEY_DISCOUNT_AMOUNT")
        val specialDiscount  = intent.getStringExtra("KEY_SPECIAL_DISCOUNT_AMOUNT")
        netTotal = intent.getStringExtra("KEY_NET_TOTAL")
        val referenceNo = intent.getStringExtra("KEY_REFERENCE_NUMBER")

        tvValueTotal.text = netTotal
        tvValueDiscountAmount.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(MathUtils.toDouble(specialDiscount))
        tvValueCash.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)
        tvValueCredit.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)
        calculateBalance(netTotal)

        tvValueDiscountAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                calculateBalance(netTotal)
            }
        })

        tvValueCash.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                calculateBalance(netTotal)
            }
        })

        tvValueCredit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                calculateBalance(netTotal)
            }
        })

        fabSave.setOnClickListener {
            var cash = tvValueCash.text.toString()
            var card = tvValueCredit.text.toString()
            val balance = tvValueBalance.text.toString()

            val balanceAmount = MathUtils.toDouble(balance)

            if (balanceAmount < 0.0) {
                AlertDialog.Builder(this)
                        .setMessage("Please check your amount")
                        .setPositiveButton("OK") { p0, p1 ->
                        }
                        .show()
                return@setOnClickListener
            }

            if (balanceAmount != 0.0 && customerId == "1") {
                AlertDialog.Builder(this)
                        .setMessage("Balance amount not allowed to cash customer !")
                        .setPositiveButton("OK") { p0, p1 ->
                        }
                        .show()
                return@setOnClickListener
            }

            cash = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(MathUtils.toDouble(cash))
            card = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(MathUtils.toDouble(card))

            uploadData(isUpdate,
                    invoiceId,
                    list,
                    date,
                    customerId,
                    name,
                    address,
                    phone,
                    total,
                    taxAmount,
                    discount,
                    netTotal,
                    cash,
                    card,
                    balance,
                    referenceNo
            )
        }

        fabPrint.setOnClickListener {
            if (fabSave.visibility == View.VISIBLE) {
                AlertDialog.Builder(this)
                        .setMessage("Please save the bill")
                        .setPositiveButton("OK") { p0, p1 ->

                        }
                        .show()
                return@setOnClickListener
            } else {
                doPrint()
            }
        }

        fabSaveAsPDF.setOnClickListener {
            if (fabSave.visibility == View.VISIBLE) {
                AlertDialog.Builder(this)
                        .setMessage("Please save the bill")
                        .setPositiveButton("OK") { p0, p1 ->

                        }
                        .show()
                return@setOnClickListener
            } else {
                saveAsPDF()
            }
        }

        fabCancel.setOnClickListener {
            if (fabSave.visibility == View.VISIBLE) {
                AlertDialog.Builder(this)
                        .setMessage("Do you want to cancel this bill?")
                        .setPositiveButton("CANCEL") { p0, p1 ->
                            finishAffinity()
                        }
                        .setNegativeButton("NO") { p0, p1 ->

                        }
                        .show()
                return@setOnClickListener
            } else {
                AlertDialog.Builder(this)
                        .setMessage("Exit?")
                        .setPositiveButton("YES") { p0, p1 ->
                            if (bt.isTurning() || dbp.isDiscovering() || (printTask != null && printTask!!.status == AsyncTask.Status.RUNNING)) {
                                AlertDialog.Builder(this)
                                        .setMessage("Do you want to cancel printing")
                                        .setPositiveButton("YES") { p0, p1 ->
                                            if (bt.isTurning())
                                                bt.cancelListener(this@SalesReturnFinishActivity)
                                            if (dbp.isDiscovering())
                                                dbp.cancelDiscovery()
                                            if (printTask != null && printTask!!.status == AsyncTask.Status.RUNNING) {
                                                printTask!!.cancel(true)
                                            }
                                            finish()
                                        }
                                        .setNegativeButton("CANCEL") { p0, p1 -> }
                                        .show()
                            } else {
                                finish()
                            }
                        }
                        .setNegativeButton("CANCEL") { p0, p1 -> }
                        .show()
            }
        }
    }

    private fun saveAsPDF() {
        pbLoadingSaveAsPDF.visibility = View.VISIBLE
        fabSaveAsPDF.isEnabled = false

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("InvoiceID", ivId)
                .add("Key", "2")

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
                        SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp", "save invoice $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoadingSaveAsPDF.visibility = View.GONE
                            fabSaveAsPDF.isEnabled = true

                            SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, message)
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoadingSaveAsPDF.visibility = View.GONE
                            fabSaveAsPDF.isEnabled = true
                            SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc", "save as pdf" + e.message)
                    runOnUiThread {
                        pbLoadingSaveAsPDF.visibility = View.GONE
                        fabSaveAsPDF.isEnabled = true
                        SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, "$resp")
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

    private fun calculateBalance(netTotal: String) {
        val discountAmount = MathUtils.toDouble(tvValueDiscountAmount.text.toString())
        val totalAmount = netTotal.toDouble() - discountAmount
        val credit = MathUtils.toDouble(tvValueCredit.text)
        val cash = MathUtils.toDouble(tvValueCash.text)
        val balance = totalAmount - (credit + cash)
        tvValueBalance.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(balance)
    }

    private fun uploadData(isUpdate: Boolean,
                           invoiceId: String,
                           list: ArrayList<ProductModel>,
                           date: String,
                           customerId: String,
                           name: String,
                           address: String,
                           phone: String,
                           total: String,
                           taxAmount: String,
                           discount: String,
                           netTotal: String,
                           cash: String,
                           card: String,
                           balance: String,
                           referenceNo: String) {
        pbLoadingSave.visibility = View.VISIBLE
        fabSave.isEnabled = false

        val client = OkHttpUtils.getOkHttpClient()

        val specialDiscount =  ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(MathUtils.toDouble(tvValueDiscountAmount.text.toString()))

        val jaSalesSummery = JSONArray()
        val joSalesSummery = JSONObject()
        joSalesSummery.put("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
        joSalesSummery.put("CustomerID", customerId)
        joSalesSummery.put("InvoiceDate", date)
        joSalesSummery.put("CustomerName", name)
        joSalesSummery.put("CustomerAddress", address)
        joSalesSummery.put("CustomerPhone", phone)
        joSalesSummery.put("GROSS_AMOUNT", total)
        joSalesSummery.put("DISCOUNT_AMOUNT", specialDiscount)
        joSalesSummery.put("TAX_AMOUNT", taxAmount)
        joSalesSummery.put("NET_AMOUNT", netTotal)
        joSalesSummery.put("CASH_AMOUNT", cash)
        joSalesSummery.put("CARD_AMOUNT", card)
        joSalesSummery.put("BALANCE_AMOUNT", balance)
        jaSalesSummery.put(joSalesSummery)

        val jaSalesDetails = JSONArray()
        for (i in 0 until list.size) {
            val joSalesDetails = JSONObject()
            joSalesDetails.put("ProductID", list[i].productId)
            joSalesDetails.put("ProductCode", list[i].productCode)
            joSalesDetails.put("Qty", ("%.3f").format(list[i].quantity))
            joSalesDetails.put("Rate", ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(list[i].rate1))
            joSalesDetails.put("DiscountAmt", ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(list[i].discountAmount))
            joSalesDetails.put("DiscountPercent", ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(list[i].discountPercentage))
            joSalesDetails.put("Tax", ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(list[i].taxAmount))
            joSalesDetails.put("Total", ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(list[i].grandTotal))
            jaSalesDetails.put(joSalesDetails)
        }

        Log.e("_______________req", "save invoice invoice id :$invoiceId")
        Log.e("_______________req", "save invoice referenceNo :$referenceNo")
        Log.e("_______________req", "save invoice sales return summery :" + jaSalesSummery.toString())
        Log.e("_______________req", "save invoice sales return details :" + jaSalesDetails.toString())

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("SalesSummary", jaSalesSummery.toString())
                .add("SalesDetails", jaSalesDetails.toString())
                .add("InvReference", referenceNo)

        if (isUpdate) {
            body.add("InvoiceID", invoiceId)
        }

        val url = "https://api.myjson.com/bins/1elzb0"
        val request = Request.Builder()
                .url(if (isUpdate) PublicUrls.URL_EDIT_SALES_RETURN_INVOICE else PublicUrls.URL_SAVE_SALES_RETURN_INVOICE)
                .post(body.build())
                .build()

        callSaveInvoice = client.newCall(request)
        callSaveInvoice?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("_____________error", " save invoice : ${e?.message}")
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoadingSave.visibility = View.GONE
                    fabSave.isEnabled = true
                    if (e is UnknownHostException)
                        SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp", "save invoice $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val data = jo.getJSONObject("Data")
                        ivId = data.getString("InvoiceID")
                        ivNo = data.getString("InvoiceNumber")
                        val message = jo.getString("Message")

                        runOnUiThread {
                            pbLoadingSave.visibility = View.GONE
                            fabSave.visibility = View.GONE
                            tvValueCash.isEnabled = false
                            tvValueCredit.isEnabled = false

                            if (!isUpdate)
                                DataCollections.getInstance(this@SalesReturnFinishActivity).addSalesReturnInvoice(ivId, ivNo, TypeManager.INVOICE_TYPE_OLD)
                            SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, message)
                            setResult(Activity.RESULT_OK)
                        }

                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoadingSave.visibility = View.GONE
                            fabSave.isEnabled = true
                            SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc", "save invoice " + e.message)
                    runOnUiThread {
                        pbLoadingSave.visibility = View.GONE
                        fabSave.isEnabled = true
                        SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, "$resp")
                    }
                }
            }
        })
    }

    private val bt = BluetoothUtils(object : BluetoothUtils.BluetoothListener {
        override fun onSuccess(isOn: Boolean) {
            if (isOn) {
                dbp.discover(this@SalesReturnFinishActivity)
            }
        }

        override fun onError() {
            fabPrint.isEnabled = true
            pbLoadingPrint.visibility = View.GONE
            SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, "Cant turn on bluetooth")
        }
    })

    private val dbp = BluetoothPrinterDiscovery(object : BluetoothPrinterDiscovery.PrinterDiscoverListener {
        override fun onSuccess(discoveredPrinter: DiscoveredPrinter) {
            val connection = discoveredPrinter.connection
            try {
                connection.open()
                val zebraPrinter = ZebraPrinterFactory.getInstance(connection)

                if (zebraPrinter.currentStatus.isReadyToPrint || zebraPrinter.currentStatus.isPaused) {
                    val header = StringUtils.derialize(DataCollections.getInstance(this@SalesReturnFinishActivity).getUser()!!.header)
                    val footer = StringUtils.derialize(DataCollections.getInstance(this@SalesReturnFinishActivity).getUser()!!.footer)
                    val cash = ("%." + DataCollections.getInstance(this@SalesReturnFinishActivity).getUser()!!.amountType + "f").format(MathUtils.toDouble(tvValueCash.text.toString()))
                    val card = ("%." + DataCollections.getInstance(this@SalesReturnFinishActivity).getUser()!!.amountType + "f").format(MathUtils.toDouble(tvValueCredit.text.toString()))
                    val balanceAmt = tvValueBalance.text.toString()
                    val discountAmt =  ("%." + DataCollections.getInstance(this@SalesReturnFinishActivity).getUser()!!.amountType + "f").format(MathUtils.toDouble(tvValueDiscountAmount.text.toString()))
                    val amtType = DataCollections.getInstance(this@SalesReturnFinishActivity).getUser()!!.amountType
                    printTask = PrintTask(printResult, connection, header, footer, SalesDataModel(date,ivNo,name,list,total,taxAmount,netTotal,cash,card,amtType,discountAmt,balanceAmt), zebraPrinter, PrintTask.FORMAT_SALES)
                    printTask!!.execute()
                } else {
                    val message = ZErr.getMessage(zebraPrinter.currentStatus)
                    SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, message)
                    fabPrint.isEnabled = true
                    pbLoadingPrint.visibility = View.GONE
                }
            } catch (e: Exception) {
                fabPrint.isEnabled = true
                pbLoadingPrint.visibility = View.GONE
                SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, "Can't connect to printer")
            }
        }

        override fun onFail() {
            fabPrint.isEnabled = true
            pbLoadingPrint.visibility = View.GONE
            SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, "Printer not found")
        }
    })

    private val printResult = object : PrintTask.ZebraPrinterNotify {
        override fun notifyChange() {
            fabPrint.isEnabled = true
            pbLoadingPrint.visibility = View.GONE
            SnackBarUtils.showSnackBar(this@SalesReturnFinishActivity, "Print success")
        }
    }

    private var printTask: PrintTask? = null

    private var isExit = false
    override fun onBackPressed() {
        if (isExit) {
            super.onBackPressed()
        } else {
            if (fabSave.visibility == View.GONE) {
                AlertDialog.Builder(this)
                        .setMessage("Exit?")
                        .setPositiveButton("YES") { p0, p1 ->
                            if (bt.isTurning() || dbp.isDiscovering() || (printTask != null && printTask!!.status == AsyncTask.Status.RUNNING)) {
                                AlertDialog.Builder(this)
                                        .setMessage("Do you want to cancel printing")
                                        .setPositiveButton("YES") { p0, p1 ->
                                            isExit = true
                                            this@SalesReturnFinishActivity.onBackPressed()
                                            if (bt.isTurning())
                                                bt.cancelListener(this@SalesReturnFinishActivity)
                                            if (dbp.isDiscovering())
                                                dbp.cancelDiscovery()
                                            if (printTask != null && printTask!!.status == AsyncTask.Status.RUNNING) {
                                                printTask!!.cancel(true)
                                            }
                                        }
                                        .setNegativeButton("CANCEL") { p0, p1 -> }
                                        .show()
                            } else {
                                isExit = true
                                this@SalesReturnFinishActivity.onBackPressed()
                            }
                        }
                        .setNegativeButton("CANCEL") { p0, p1 -> }
                        .show()
            } else {
                isExit = true
                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callSaveInvoice)
        OkHttpUtils.cancelCalls(callSaveAsPDF)
    }

    private fun init() {
        layoutParent = findViewById(R.id.layoutParent)
        fabSave = findViewById(R.id.fabSave)
        fabSaveAsPDF = findViewById(R.id.fabSaveAsPDF)
        pbLoadingSaveAsPDF = findViewById(R.id.pbLoadingSaveAsPDF)
        pbLoadingPrint = findViewById(R.id.pbLoadingPrint)
        fabPrint = findViewById(R.id.fabPrint)
        fabCancel = findViewById(R.id.fabCancel)
        pbLoadingSave = findViewById(R.id.pbLoadingSave)
        tvValueTotal = findViewById(R.id.tvValueTotal)
        tvValueDiscountAmount = findViewById(R.id.tvValueDiscountAmount)
        tvValueCash = findViewById(R.id.tvValueCash)
        tvValueCredit = findViewById(R.id.tvValueCredit)
        tvValueBalance = findViewById(R.id.tvValueBalance)
    }

}
