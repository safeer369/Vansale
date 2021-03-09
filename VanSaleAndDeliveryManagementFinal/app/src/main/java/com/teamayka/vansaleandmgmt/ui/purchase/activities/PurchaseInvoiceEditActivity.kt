package com.teamayka.vansaleandmgmt.ui.purchase.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.teamayka.vansaleandmgmt.utils.PermissionUtils
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import com.teamayka.vansaleandmgmt.ui.main.models.ProductModel
import com.teamayka.vansaleandmgmt.ui.main.models.SupplierModel
import com.teamayka.vansaleandmgmt.ui.purchase.adapters.PurchaseRvAdapter
import com.teamayka.vansaleandmgmt.utils.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class PurchaseInvoiceEditActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSION_CAMERA = 10
        private const val DELAY_BARCODE_RESCAN = 1000L
    }

    private lateinit var layoutParent: View
    private lateinit var cbCredit: CheckBox
    private lateinit var tvItemCount: TextView
    private lateinit var ivRead: ImageView
    private lateinit var etInvoiceNo: AutoCompleteTextView
    private lateinit var etInvoiceDate: EditText
    private lateinit var pbLoadingInvoices: ProgressBar
    private lateinit var rv: RecyclerView
    private lateinit var pbLoadingInvoiceDetails: ProgressBar
    private lateinit var tvMessage: TextView
    private lateinit var tvRetry: TextView
    private lateinit var fabFinish: FloatingActionButton
    private lateinit var etCustomerName: AutoCompleteTextView
    private lateinit var etCustomerAddress: EditText
    private lateinit var etPhoneNo: EditText
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var tvValueTotal: TextView
    private lateinit var tvValueTaxAmount: TextView
    private lateinit var tvValueDiscount: TextView
    private lateinit var tvValueNetTotal: TextView

    val productItemsList = ArrayList<ProductModel>() // recycler view list
    private val invoiceNumberList = ArrayList<String>() // list of invoice numbers
    private val invoiceIdList = ArrayList<String>() // list of invoice ids

    var productList = java.util.ArrayList<ProductModel>()
    private val productListNames = java.util.ArrayList<String>()

    var customerList = java.util.ArrayList<SupplierModel>()
    private val customerListNames = java.util.ArrayList<String>()

    private var callGetInvoices: Call? = null
    private var callGetInvoiceDetails: Call? = null

    var ratePlan = 0 // default
    private var customerId = "1" // used to upload data

    private var total = 0.0
    private var taxAmount = 0.0
    private var discountAmount = 0.0
    private var netTotal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_invoice_edit)
        init()

        tvValueTotal.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)
        tvValueTaxAmount.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)
        tvValueDiscount.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)
        tvValueNetTotal.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(0.0)

        productList = DataCollections.getInstance(this).getProducts()
        for (i in 0 until productList.size) {
            productListNames.add(productList[i].displayName)
        }

        customerList = DataCollections.getInstance(this).getSupplier()
        for (i in 0 until customerList.size) {
            customerListNames.add(customerList[i].name)
        }

        rv.addItemDecoration(RvItemMargin(10))

        etInvoiceNo.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && invoiceNumberList.size > 0) {
                AlertDialog.Builder(this)
                        .setMessage("If you are editing invoice. Data will lose on choose another one !")
                        .setPositiveButton("GOT IT") { p0, p1 -> }
                        .show()
            }
            if (hasFocus && invoiceNumberList.size == 0) {
                if (callGetInvoices == null) {
                    getInvoices()
                }
            }
        }
    }

    private fun getInvoices() {
        pbLoadingInvoices.visibility = View.VISIBLE

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .build()

        val request = Request.Builder()
                .url(PublicUrls.URL_GET_PURCHASE_INVOICES)
                .post(body)
                .build()

        callGetInvoices = client.newCall(request)
        callGetInvoices?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoadingInvoices.visibility = View.GONE
                    callGetInvoices = null // OK HTTP WILL NOT BE NULL IF CALL FAILED. I WANT CHECK IS IT NULL
                    if (e is UnknownHostException)
                        SnackBarUtils.showSnackBar(this@PurchaseInvoiceEditActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@PurchaseInvoiceEditActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp", "_get_invoices : $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val jaData = jo.getJSONArray("Data")
                        for (i in 0 until jaData.length()) {
                            val joData = jaData.getJSONObject(i)
                            val invoiceNumber = joData.getString("InvoiceNumber")
                            val invoiceId = joData.getString("InvoiceID")
                            invoiceNumberList.add(invoiceNumber)
                            invoiceIdList.add(invoiceId)
                        }

                        runOnUiThread {
                            pbLoadingInvoices.visibility = View.GONE
                            callGetInvoices = null

                            etInvoiceNo.threshold = 1
                            etInvoiceNo.setAdapter(ArrayAdapter<String>(this@PurchaseInvoiceEditActivity, android.R.layout.simple_list_item_1, invoiceNumberList))
                            etInvoiceNo.setOnItemClickListener { parent, v, i1, l ->
                                // where the position i1 is invalid so this technique was used
                                val vl = parent.getItemAtPosition(i1) as String
                                val i = invoiceNumberList.indexOf(vl)
                                val invoiceId = invoiceIdList[i]
                                val invoiceNo = invoiceNumberList[i]
                                val aa = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                aa.hideSoftInputFromWindow(etInvoiceNo.windowToken, 0)
                                getInvoiceDetails(invoiceId, invoiceNo)
                            }
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoadingInvoices.visibility = View.GONE
                            callGetInvoices = null
                            SnackBarUtils.showSnackBar(this@PurchaseInvoiceEditActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc", "_get_invoices : " + e.message)
                    runOnUiThread {
                        pbLoadingInvoices.visibility = View.GONE
                        callGetInvoices = null
                        SnackBarUtils.showSnackBar(this@PurchaseInvoiceEditActivity, "$resp")
                    }
                }
            }
        })
    }

    private fun getInvoiceDetails(invoiceId: String, invoiceNo: String) {
        tvMessage.visibility = View.GONE
        tvRetry.visibility = View.GONE
        rv.visibility = View.INVISIBLE
        pbLoadingInvoiceDetails.visibility = View.VISIBLE
        etCustomerName.isEnabled = false
        etCustomerAddress.isEnabled = false
        etPhoneNo.isEnabled = false

        tvRetry.setOnClickListener {
            getInvoiceDetails(invoiceId, invoiceNo)
        }

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("InvoiceID", invoiceId)
                .add("InvoiceNumber", invoiceNo)
                .build()

        Log.e("_______________req", "get invoice details __ InvoiceID : $invoiceId InvoiceNumber : $invoiceNo")

        val request = Request.Builder()
                .url(PublicUrls.URL_GET_PURCHASE_INVOICE_DETAILS)
                .post(body)
                .build()

        callGetInvoiceDetails = client.newCall(request)
        callGetInvoiceDetails?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoadingInvoiceDetails.visibility = View.GONE
                    rv.visibility = View.INVISIBLE
                    tvMessage.visibility = View.VISIBLE
                    tvRetry.visibility = View.VISIBLE
                    etCustomerName.isEnabled = true
                    etCustomerAddress.isEnabled = true
                    etPhoneNo.isEnabled = true

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
                Log.e("_____________resp", "get invoice details : $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val jaInvoiceDetails = jo.getJSONArray("PurchaseDetails")
                        productItemsList.clear()
                        for (i in 0 until jaInvoiceDetails.length()) {
                            val joInvoiceDetails = jaInvoiceDetails.getJSONObject(i)
                            val productId = joInvoiceDetails.getString("ProductID")
                            val productCode = joInvoiceDetails.getString("ProductCode")
                            val productName = joInvoiceDetails.getString("ProductName")
                            val displayName = joInvoiceDetails.getString("DisplayName")
                            val unit = joInvoiceDetails.getString("Unit")
                            val unitId = joInvoiceDetails.getString("UnitID")
                            val qty = joInvoiceDetails.getDouble("Qty")
                            val rate = joInvoiceDetails.getDouble("Rate")
                            val cGST = joInvoiceDetails.getDouble("CGST")
                            val sGST = joInvoiceDetails.getDouble("SGST")
                            val taxInclusive = joInvoiceDetails.getString("TaxInclusive")
                            val discountAmt = joInvoiceDetails.getDouble("DiscountAmt")
                            val discountPercent = joInvoiceDetails.getDouble("DiscountPercent")
                            val tax = joInvoiceDetails.getDouble("Tax")
                            val total = joInvoiceDetails.getDouble("Total")
                            val imageUrl = joInvoiceDetails.getString("ImageUrl")
                            productItemsList.add(ProductModel(productId,
                                    productCode,
                                    productName,
                                    displayName,
                                    unit,
                                    unitId,
                                    cGST,
                                    sGST,
                                    rate,
                                    rate,
                                    rate,
                                    taxInclusive,
                                    imageUrl,
                                    qty,
                                    discountPercent,
                                    discountAmt,
                                    total,
                                    tax,
                                    total + tax))
                        }
                        val joSummary = jo.getJSONObject("Summary")
                        val invoiceNumber = joSummary.getString("InvoiceNumber")
                        val userId = joSummary.getString("UserID")
                        val customerId = joSummary.getString("SupplierID")
                        val invoiceDate = joSummary.getString("PurchaseDate")
                        val customerName = joSummary.getString("SupplierName")
                        val customerAddress = joSummary.getString("SupplierAddress")
                        val customerPhone = joSummary.getString("SupplierPhone")
                        val grossAmount = joSummary.getString("GROSS_AMOUNT")
                        val discountAmount = joSummary.getString("DISCOUNT_AMOUNT")
                        val taxAmount = joSummary.getString("TAX_AMOUNT")
                        val netAmount = joSummary.getString("NET_AMOUNT")
                        val cashAmount = joSummary.getString("CASH_AMOUNT")
                        val cardAmount = joSummary.getString("CARD_AMOUNT")
                        val balanceAmount = joSummary.getString("BALANCE_AMOUNT")
                        val specialDiscount = joSummary.getString("SPECIAL_DISCOUNT")

                        runOnUiThread {
                            pbLoadingInvoiceDetails.visibility = View.GONE
                            tvMessage.visibility = View.GONE
                            tvRetry.visibility = View.GONE
                            rv.visibility = View.VISIBLE

                            showInvoiceDetails(
                                    customerId,
                                    invoiceId,
                                    invoiceNumber,
                                    invoiceDate,
                                    customerName,
                                    customerAddress,
                                    customerPhone,
                                    netAmount,
                                    taxAmount,
                                    discountAmount,
                                    grossAmount,
                                    specialDiscount)
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            rv.visibility = View.INVISIBLE
                            pbLoadingInvoiceDetails.visibility = View.GONE
                            tvMessage.visibility = View.VISIBLE
                            tvRetry.visibility = View.VISIBLE
                            tvMessage.text = message
                            etCustomerName.isEnabled = true
                            etCustomerAddress.isEnabled = true
                            etPhoneNo.isEnabled = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc", "get invoice details" + e.message)
                    runOnUiThread {
                        rv.visibility = View.INVISIBLE
                        pbLoadingInvoiceDetails.visibility = View.GONE
                        tvMessage.visibility = View.VISIBLE
                        tvRetry.visibility = View.VISIBLE
                        tvMessage.text = "$resp"
                        etCustomerName.isEnabled = true
                        etCustomerAddress.isEnabled = true
                        etPhoneNo.isEnabled = true
                    }
                }
            }
        })
    }

    private fun showInvoiceDetails(customerId: String,
                                   invoiceId: String,
                                   invoiceNumber: String,
                                   invoiceDate: String,
                                   customerName: String,
                                   customerAddress: String,
                                   customerPhone: String,
                                   netAmount: String,
                                   taxAmount: String,
                                   discountAmount: String,
                                   grossAmount: String,
                                   specialDiscount: String) {
        this.customerId = customerId
        etInvoiceNo.setText(invoiceNumber)
        etInvoiceDate.setText(invoiceDate.split(" ")[0])

        etCustomerName.isEnabled = true
        etCustomerAddress.isEnabled = true
        etPhoneNo.isEnabled = true
        fabFinish.isEnabled = true
        ivRead.isEnabled = true
        fabAddItem.isEnabled = true
        cbCredit.isEnabled = true

        cbCredit.isChecked = customerId != "1"
        etCustomerName.setText(customerName)
        etCustomerAddress.setText(customerAddress)
        etPhoneNo.setText(customerPhone)

        this.discountAmount = MathUtils.toDouble(discountAmount)
        this.taxAmount = MathUtils.toDouble(taxAmount)
        this.netTotal = MathUtils.toDouble(netAmount)
        this.total = MathUtils.toDouble(grossAmount)

        tvValueNetTotal.text = netAmount
        tvValueTaxAmount.text = taxAmount
        tvValueDiscount.text = discountAmount
        tvValueTotal.text = grossAmount

        rv.adapter = PurchaseRvAdapter(
                this,
                productItemsList,
                productList,
                productListNames,
                tvItemCount,
                etInvoiceNo,
                etCustomerName,
                etCustomerAddress,
                etPhoneNo,
                cbCredit)

        fabFinish.setOnClickListener {
            if (!isValidAll(customerList))
                return@setOnClickListener

            if (productItemsList.size == 0) {
                SnackBarUtils.showSnackBar(this, "Please add products")
                return@setOnClickListener
            }

            val intent = Intent(this, PurchaseBillActivity::class.java)
            intent.putExtra("KEY_IS_UPDATE", true)
            intent.putExtra("KEY_INVOICE_ID", invoiceId)
            intent.putParcelableArrayListExtra("KEY_LIST", productItemsList)
            intent.putExtra("KEY_DATE", etInvoiceDate.text.toString())
            if (cbCredit.isChecked)
                intent.putExtra("KEY_CUSTOMER_ID", customerId)
            else
                intent.putExtra("KEY_CUSTOMER_ID", "1") // will be cash customer
            intent.putExtra("KEY_NAME", etCustomerName.text.toString())
            intent.putExtra("KEY_ADDRESS", etCustomerAddress.text.toString())
            intent.putExtra("KEY_PHONE", etPhoneNo.text.toString())
            intent.putExtra("KEY_TOTAL", ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(this.total))
            intent.putExtra("KEY_TAX_AMOUNT", ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(this.taxAmount))
            intent.putExtra("KEY_DISCOUNT_AMOUNT", ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(this.discountAmount))
            intent.putExtra("KEY_SPECIAL_DISCOUNT_AMOUNT", specialDiscount)
            intent.putExtra("KEY_NET_TOTAL", ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(this.netTotal))
            startActivity(intent)
        }

        etCustomerName.threshold = 1
        etCustomerName.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, customerListNames))
        etCustomerName.setOnItemClickListener { adapterView, v, i1, l ->
            // todo if something edit here, that edit should apply in two places in SalesFragment
            // where the position i1 is invalid so this technique was used
            val vl = adapterView.getItemAtPosition(i1) as String
            val position = customerListNames.indexOf(vl)

            Tools.hideSoftKeyboard(this)

            this.customerId = customerList[position].customerId
            etCustomerAddress.setText(customerList[position].address1)
            etPhoneNo.setText(customerList[position].telephone)
            ratePlan = customerList[position].ratePlan.toInt()
            cbCredit.isChecked = this.customerId != "1"

            for (iL in 0 until productItemsList.size) {
                val itemPos = productList.indexOfFirst { it -> it.productCode == productItemsList[iL].productCode }
                if (itemPos == -1)
                    continue
                if (productItemsList[iL].rate1 == productList[itemPos].rate1 ||
                        productItemsList[iL].rate1 == productList[itemPos].rate2 ||
                        productItemsList[iL].rate1 == productList[itemPos].rate3) {
                    productItemsList[iL].rate1 = when (ratePlan) {
                        0 -> productList[itemPos].rate1
                        1 -> productList[itemPos].rate2
                        else -> productList[itemPos].rate3
                    }
                    calculateAmounts(iL, productItemsList[iL].rate1, productItemsList[iL].discountPercentage, productItemsList[iL].sGSTPercentage, productItemsList[iL].cGSTPercentage, productItemsList[iL].quantity)
                }
            }
            rv.adapter.notifyDataSetChanged()
            calculateTotal()
        }

        ivRead.setOnClickListener {
            if (!isValidAll(customerList))
                return@setOnClickListener

            val cameraPermission = arrayOf(android.Manifest.permission.CAMERA)
            if (PermissionUtils.hasAllPermissions(this, cameraPermission)) {
                startRead()
            } else {
                // FOR FRAGMENT USE requestPermissions function NOT ActivityCompat.requestPermissions
                ActivityCompat.requestPermissions(this, cameraPermission, REQUEST_CODE_PERMISSION_CAMERA)
            }
        }

        fabAddItem.setOnClickListener {
            if (!isValidAll(customerList))
                return@setOnClickListener

            productItemsList.add(ProductModel("",
                    "",
                    "",
                    "",
                    "",
                    "",
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "",
                    "",
                    1.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0
            ))
            rv.adapter.notifyItemInserted(productItemsList.size - 1)
            rv.scrollToPosition(productItemsList.size - 1)
        }
    }

    private fun calculateAmounts(position: Int, rate: Double, discountPercentage: Double, sGSTPercentage: Double, cGSTPercentage: Double, quantity: Double) {
        val totalRate = rate * quantity // calculate rate as quantity
        val discountAmount = totalRate * discountPercentage / 100
        val totalAmount = totalRate - discountAmount
        val taxAmount = totalAmount * (sGSTPercentage + cGSTPercentage) / 100
        val grandTotal = totalAmount + taxAmount
        productItemsList[position].discountAmount = discountAmount
        productItemsList[position].totalAmount = totalAmount
        productItemsList[position].taxAmount = taxAmount
        productItemsList[position].grandTotal = grandTotal
    }

    private fun isValidAll(customers: java.util.ArrayList<SupplierModel>): Boolean {
        val customerName = etCustomerName.text.toString().trim()
        val customerAddress = etCustomerAddress.text.toString().trim()
        val customerPhone = etPhoneNo.text.toString().trim()
        val invoiceDate = etInvoiceDate.text.toString().trim()

        if (TextUtils.isEmpty(invoiceDate)) {
            SnackBarUtils.showSnackBar(this, "Please choose a valid date")
            return false
        }

        if (cbCredit.isChecked) {
            if (!customers.any { it1 -> it1.name.trim() == customerName && it1.address1.trim() == customerAddress && it1.telephone.trim() == customerPhone && it1.customerId == customerId }) {
                SnackBarUtils.showSnackBar(this, "Please choose a valid customer")
                return false
            }

            if (customerId == "1") {
                SnackBarUtils.showSnackBar(this, "Selected customer is not a credit customer")
                return false
            }
        }

        if (productItemsList.size > 0) {
            for (i in 0 until productItemsList.size) {
                if (!productList.any { it2 -> it2.displayName == productItemsList[i].displayName }) {
                    SnackBarUtils.showSnackBar(this, "Invalid product list")
                    return false
                }
            }
        }
        return true
    }

    private inner class RvItemMargin(val margin: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect?.left = margin
            outRect?.right = margin
            outRect?.top = margin
            outRect?.bottom = margin
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_CAMERA -> {
                if (PermissionUtils.hasAllPermissionsGranted(grantResults)) {
                    startRead()
                } else {
                    SnackBarUtils.showSnackBar(this, "Camera access denied !")
                }
            }
        }
    }

    private fun startRead() {
        val v = LayoutInflater.from(this).inflate(R.layout.test_layout_dialog_reading, null)

        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(v)
        dialog.show()

        val surfaceView = v.findViewById<BarcodeScanningView>(R.id.surfaceView)
        val ivFrame = v.findViewById<ImageView>(R.id.ivFrame)
        surfaceView.setFrame(ivFrame)

        surfaceView.setCallBack(object : BarcodeScanningView.ReceiverCallBack {
            override fun onReceiveData(qrValue: String, barcodeDetector: BarcodeDetector, barcodeProcessor: BarcodeScanningView.BarcodeProcessor) {
                barcodeDetector.release()
                MediaPlayer.create(this@PurchaseInvoiceEditActivity, R.raw.beep1).start()
                if (TextUtils.isEmpty(qrValue)) {
                    runOnUiThread {
                        Snackbar.make(v, "Invalid Barcode", Snackbar.LENGTH_SHORT).show()
                        Handler().postDelayed({
                            barcodeDetector.setProcessor(barcodeProcessor)
                        }, DELAY_BARCODE_RESCAN)
                    }
                } else {
                    val isMatchProductCode = isMatch(productList, qrValue)
                    if (isMatchProductCode != null) {
                        productItemsList.add(isMatchProductCode)
                        runOnUiThread {
                            rv.adapter.notifyDataSetChanged()
                            rv.scrollToPosition(productItemsList.size - 1)
                        }
                        dialog.cancel()
                    } else {
                        runOnUiThread {
                            Snackbar.make(v, "Invalid Product", Snackbar.LENGTH_SHORT).show()
                            Handler().postDelayed({
                                barcodeDetector.setProcessor(barcodeProcessor)
                            }, DELAY_BARCODE_RESCAN)
                        }
                    }
                }
            }
        })
    }

    fun isMatch(productList: java.util.ArrayList<ProductModel>, qrValue: String): ProductModel? {
        for (i in 0 until productList.size) {
            if (productList[i].productCode == qrValue) {
                return productList[i]
            }
        }
        return null
    }

    fun calculateTotal() { // CALCULATE TOTAL OF ALL ITEMS
        total = 0.0
        taxAmount = 0.0
        discountAmount = 0.0
        netTotal = 0.0
        for (i in 0 until productItemsList.size) {
            total += productItemsList[i].totalAmount
            taxAmount += productItemsList[i].taxAmount
            discountAmount += productItemsList[i].discountAmount
            netTotal += productItemsList[i].grandTotal
        }
        tvValueTotal.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(total)
        tvValueTaxAmount.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(taxAmount)
        tvValueDiscount.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(discountAmount)
        tvValueNetTotal.text = ("%." + DataCollections.getInstance(this).getUser()!!.amountType + "f").format(netTotal)
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callGetInvoices)
        OkHttpUtils.cancelCalls(callGetInvoiceDetails)
    }

    private var isExit = false
    override fun onBackPressed() {
        if (isExit) {
            super.onBackPressed()
        } else {
            if (productItemsList.size > 0) {
                AlertDialog.Builder(this)
                        .setMessage("Product list will be destroyed. Back?")
                        .setPositiveButton("YES") { p0, p1 ->
                            isExit = true
                            this@PurchaseInvoiceEditActivity.onBackPressed()
                        }
                        .setNegativeButton("CANCEL") { p0, p1 -> }
                        .show()
            } else {
                isExit = true
                super.onBackPressed()
            }
        }
    }

    private fun init() {
        layoutParent = findViewById(R.id.layoutParent)
        cbCredit = findViewById(R.id.cbCredit)
        tvItemCount = findViewById(R.id.tvItemCount)
        ivRead = findViewById(R.id.ivRead)
        etInvoiceNo = findViewById(R.id.etInvoiceNo)
        etInvoiceDate = findViewById(R.id.etInvoiceDate)
        pbLoadingInvoices = findViewById(R.id.pbLoadingInvoices)
        rv = findViewById(R.id.rv)
        pbLoadingInvoiceDetails = findViewById(R.id.pbLoadingInvoiceDetails)
        tvMessage = findViewById(R.id.tvMessage)
        tvRetry = findViewById(R.id.tvRetry)
        fabFinish = findViewById(R.id.fabFinish)
        etCustomerName = findViewById(R.id.etCustomerName)
        etCustomerAddress = findViewById(R.id.etCustomerAddress)
        etPhoneNo = findViewById(R.id.etPhoneNo)
        fabAddItem = findViewById(R.id.fabAddItem)
        tvValueTotal = findViewById(R.id.tvValueTotal)
        tvValueTaxAmount = findViewById(R.id.tvValueTaxAmount)
        tvValueDiscount = findViewById(R.id.tvValueDiscount)
        tvValueNetTotal = findViewById(R.id.tvValueNetTotal)
    }
}
