package com.teamayka.vansaleandmgmt.ui.salesorder.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import com.teamayka.vansaleandmgmt.ui.main.models.CustomerModel
import com.teamayka.vansaleandmgmt.ui.main.models.ProductModel
import com.teamayka.vansaleandmgmt.ui.sales.activities.SalesActivity
import com.teamayka.vansaleandmgmt.ui.salesorder.activities.SalesOrderActivity
import com.teamayka.vansaleandmgmt.ui.salesorder.activities.SalesOrderBillActivity
import com.teamayka.vansaleandmgmt.ui.salesorder.activities.SalesOrderInvoiceEditActivity
import com.teamayka.vansaleandmgmt.ui.salesorder.adapters.SalesOrderRvAdapter
import com.teamayka.vansaleandmgmt.utils.*
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by seydbasil on 3/29/2018.
 */
class SalesOrderFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_PERMISSION_CAMERA = 10
        private const val DELAY_BARCODE_RESCAN = 1000L
    }

    var mContext: Context? = null
    private lateinit var layoutParent: View
    private lateinit var rv: RecyclerView
    private lateinit var cbCredit: CheckBox
    private lateinit var tvItemCount: TextView
    private lateinit var ivRead: ImageView
    private lateinit var ivSearch: ImageView
    private lateinit var fabFinish: FloatingActionButton
    private lateinit var etInvoiceNo: AutoCompleteTextView
    private lateinit var etInvoiceDate: EditText
    private lateinit var etCustomerName: AutoCompleteTextView
    private lateinit var etCustomerAddress: EditText
    private lateinit var etPhoneNo: EditText
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var tvValueTotal: TextView
    private lateinit var tvValueTaxAmount: TextView
    private lateinit var tvValueDiscount: TextView
    private lateinit var tvValueNetTotal: TextView
    private lateinit var pbLoadingInvoiceDetails: ProgressBar
    private lateinit var tvMessage: TextView
    private lateinit var tvRetry: TextView

    val productItemsList = ArrayList<ProductModel>() // recycler view list

    private var callGetInvoiceDetails: Call? = null

    var ratePlan = 0 // default
    private var customerId = "1" // used to upload data

    private var total = 0.0
    private var taxAmount = 0.0
    private var discountAmount = 0.0
    private var netTotal = 0.0

    private var position = 0
    private var invoiceId = ""
    private var invoiceNo = ""
    var invoiceStatus = ""

    var isRecyclerViewEnabled = false // USED THIS VALUE TO ENABLE AND DISABLE RECYCLERVIEW ITEM VIEW

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sales_order, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)

        tvValueTotal.text = ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(0.0)
        tvValueTaxAmount.text = ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(0.0)
        tvValueDiscount.text = ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(0.0)
        tvValueNetTotal.text = ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(0.0)

        initPage()

        position = arguments!!.getInt("KEY_POSITION")
        invoiceId = arguments!!.getString("KEY_INVOICE_ID")
        invoiceNo = arguments!!.getString("KEY_INVOICE_NO")
        invoiceStatus = arguments!!.getString("KEY_INVOICE_STATUS")

        ivSearch.setOnClickListener {
            val intent = Intent(mContext, SalesOrderInvoiceEditActivity::class.java)
            // finishAffinity finishes all activities except new task
            // in SalesBillActivity there is finishAffinity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        rv.addItemDecoration(RvItemMargin(10))

        if (invoiceStatus == TypeManager.INVOICE_TYPE_OLD) {
            getInvoiceDetails(invoiceId, invoiceNo)
        } else if (invoiceStatus == TypeManager.INVOICE_TYPE_FRESH) {
            setUpList(invoiceId, invoiceNo, invoiceStatus)
            setUpFields(invoiceId, invoiceNo, invoiceStatus)
            setUpButtons(invoiceId, invoiceNo, invoiceStatus)
        }
    }

    private fun initPage() {
        productItemsList.clear()
        customerId = "1"
        total = 0.0
        taxAmount = 0.0
        discountAmount = 0.0
        netTotal = 0.0
        callGetInvoiceDetails = null
    }

    private fun setUpFields(invoiceId: String, invoiceNo: String, invoiceStatus: String) {
        etInvoiceNo.visibility = View.GONE
        etCustomerName.threshold = 1
        etCustomerName.setAdapter(ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, (mContext as SalesOrderActivity).customerListNames))
        etCustomerName.setOnItemClickListener { adapterView, v, i1, l ->
            // where the position i1 is invalid so this technique was used
            val vl = adapterView.getItemAtPosition(i1) as String
            val position = (mContext as SalesOrderActivity).customerListNames.indexOf(vl)

            Tools.hideSoftKeyboard(mContext as Activity)

            this.customerId = (mContext as SalesOrderActivity).customerList[position].customerId
            etCustomerAddress.setText((mContext as SalesOrderActivity).customerList[position].address1)
            etPhoneNo.setText((mContext as SalesOrderActivity).customerList[position].telephone)
            ratePlan = (mContext as SalesOrderActivity).customerList[position].ratePlan.toInt()
            cbCredit.isChecked = this.customerId != "1"

            for (iL in 0 until productItemsList.size) {
                val itemPos = (mContext as SalesOrderActivity).productList.indexOfFirst { it -> it.productCode == productItemsList[iL].productCode }
                if (itemPos == -1)
                    continue
                if (productItemsList[iL].rate1 == (mContext as SalesOrderActivity).productList[itemPos].rate1 ||
                        productItemsList[iL].rate1 == (mContext as SalesOrderActivity).productList[itemPos].rate2 ||
                        productItemsList[iL].rate1 == (mContext as SalesOrderActivity).productList[itemPos].rate3) {
                    productItemsList[iL].rate1 = when (ratePlan) {
                        0 -> (mContext as SalesOrderActivity).productList[itemPos].rate1
                        1 -> (mContext as SalesOrderActivity).productList[itemPos].rate2
                        else -> (mContext as SalesOrderActivity).productList[itemPos].rate3
                    }
                    calculateAmounts(iL, productItemsList[iL].rate1, productItemsList[iL].discountPercentage, productItemsList[iL].sGSTPercentage, productItemsList[iL].cGSTPercentage, productItemsList[iL].quantity)
                }
            }
            rv.adapter.notifyDataSetChanged()
            calculateTotal()
        }

        val currentDate = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        etInvoiceDate.setText(df.format(currentDate))

        etInvoiceDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val pickerDialog = DatePickerDialog.newInstance({ view, year, monthOfYear, dayOfMonth ->
                val date = "$year-${monthOfYear + 1}-$dayOfMonth"
                etInvoiceDate.setText(date)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            pickerDialog.accentColor = ResourcesCompat.getColor(resources, R.color.colorBgDefaultBlue, null)
            pickerDialog.show((mContext as Activity).fragmentManager, "DatePickerDialog")
        }
    }

    private fun setUpButtons(invoiceId: String, invoiceNo: String, invoiceStatus: String) {
        ivRead.setOnClickListener {
            if (!isValidAll((mContext as SalesOrderActivity).customerList))
                return@setOnClickListener

            val cameraPermission = arrayOf(android.Manifest.permission.CAMERA)
            if (PermissionUtils.hasAllPermissions(mContext!!, cameraPermission)) {
                startRead()
            } else {
                // FOR FRAGMENT USE requestPermissions function NOT ActivityCompat.requestPermissions
                requestPermissions(cameraPermission, REQUEST_CODE_PERMISSION_CAMERA)
            }
        }

        fabAddItem.setOnClickListener {
            if (!isValidAll((mContext as SalesOrderActivity).customerList))
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

        fabFinish.setOnClickListener {
            if (!isValidAll((mContext as SalesOrderActivity).customerList))
                return@setOnClickListener

            if (productItemsList.size == 0) {
                SnackBarUtils.showSnackBar(mContext as Activity, "Please add products")
                return@setOnClickListener
            }

            val intent = Intent(mContext, SalesOrderBillActivity::class.java)
            intent.putExtra("KEY_IS_UPDATE", false)
            intent.putParcelableArrayListExtra("KEY_LIST", productItemsList)
            intent.putExtra("KEY_DATE", etInvoiceDate.text.toString())
            if (cbCredit.isChecked)
                intent.putExtra("KEY_CUSTOMER_ID", customerId)
            else
                intent.putExtra("KEY_CUSTOMER_ID", "1") // will be cash customer
            intent.putExtra("KEY_NAME", etCustomerName.text.toString())
            intent.putExtra("KEY_ADDRESS", etCustomerAddress.text.toString())
            intent.putExtra("KEY_PHONE", etPhoneNo.text.toString())
            intent.putExtra("KEY_TOTAL", ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(total))
            intent.putExtra("KEY_TAX_AMOUNT", ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(taxAmount))
            intent.putExtra("KEY_DISCOUNT_AMOUNT", ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(discountAmount))
            intent.putExtra("KEY_NET_TOTAL", ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(netTotal))
            startActivity(intent)
        }
    }

    private fun setUpList(invoiceId: String, invoiceNo: String, invoiceStatus: String) {
        isRecyclerViewEnabled = true
        rv.visibility = View.VISIBLE
        rv.adapter = SalesOrderRvAdapter(
                this,
                productItemsList,
                (mContext as SalesOrderActivity).productList,
                (mContext as SalesOrderActivity).productListNames,
                tvItemCount,
                etInvoiceNo,
                etCustomerName,
                etCustomerAddress,
                etPhoneNo,
                cbCredit)
    }

    private fun getInvoiceDetails(invoiceId: String, invoiceNo: String) {
        tvMessage.visibility = View.GONE
        tvRetry.visibility = View.GONE
        rv.visibility = View.INVISIBLE
        pbLoadingInvoiceDetails.visibility = View.VISIBLE

        fabFinish.setImageResource(R.drawable.ic_edit_white)
        etInvoiceNo.isEnabled = false
        etInvoiceDate.isEnabled = false

        fabFinish.isEnabled = false
        isViewsEnabled(false)

        tvRetry.setOnClickListener {
            getInvoiceDetails(invoiceId, invoiceNo)
        }

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(mContext!!).getUser()?.id ?: "")
                .add("InvoiceID", invoiceId)
                .add("InvoiceNumber", invoiceNo)
                .build()

        Log.e("_______________req", "get invoice details __ InvoiceID : $invoiceId InvoiceNumber : $invoiceNo")

        val request = Request.Builder()
                .url(PublicUrls.URL_GET_SALES_ORDER_INVOICE_DETAILS)
                .post(body)
                .build()

        callGetInvoiceDetails = client.newCall(request)
        callGetInvoiceDetails?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                (mContext as Activity).runOnUiThread {
                    pbLoadingInvoiceDetails.visibility = View.GONE
                    rv.visibility = View.INVISIBLE
                    tvMessage.visibility = View.VISIBLE
                    tvRetry.visibility = View.VISIBLE

                    fabFinish.isEnabled = false
                    isViewsEnabled(false)
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
                        val jaInvoiceDetails = jo.getJSONArray("InvoiceDetails")
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
                        val customerId = joSummary.getString("CustomerID")
                        val invoiceDate = joSummary.getString("InvoiceDate")
                        val customerName = joSummary.getString("CustomerName")
                        val customerAddress = joSummary.getString("CustomerAddress")
                        val customerPhone = joSummary.getString("CustomerPhone")
                        val grossAmount = joSummary.getString("GROSS_AMOUNT")
                        val discountAmount = joSummary.getString("DISCOUNT_AMOUNT")
                        val taxAmount = joSummary.getString("TAX_AMOUNT")
                        val netAmount = joSummary.getString("NET_AMOUNT")
                        val cashAmount = joSummary.getString("CASH_AMOUNT")
                        val cardAmount = joSummary.getString("CARD_AMOUNT")
                        val balanceAmount = joSummary.getString("BALANCE_AMOUNT")
                        val specialDiscount = joSummary.getString("SPECIAL_DISCOUNT")

                        (mContext as Activity).runOnUiThread {
                            pbLoadingInvoiceDetails.visibility = View.GONE
                            tvMessage.visibility = View.GONE
                            tvRetry.visibility = View.GONE
                            rv.visibility = View.VISIBLE

                            setUpOldFragment(
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
                        (mContext as Activity).runOnUiThread {
                            rv.visibility = View.INVISIBLE
                            pbLoadingInvoiceDetails.visibility = View.GONE
                            tvMessage.visibility = View.VISIBLE
                            tvRetry.visibility = View.VISIBLE
                            tvMessage.text = message

                            fabFinish.isEnabled = false
                            isViewsEnabled(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc", "get invoice details" + e.message)
                    (mContext as Activity).runOnUiThread {
                        rv.visibility = View.INVISIBLE
                        pbLoadingInvoiceDetails.visibility = View.GONE
                        tvMessage.visibility = View.VISIBLE
                        tvRetry.visibility = View.VISIBLE
                        tvMessage.text = "$resp"

                        fabFinish.isEnabled = false
                        isViewsEnabled(false)
                    }
                }
            }
        })
    }

    private fun setUpOldFragment(customerId: String, invoiceId: String, invoiceNumber: String, invoiceDate: String, customerName: String, customerAddress: String, customerPhone: String, netAmount: String, taxAmount: String, discountAmount: String, grossAmount: String, specialDiscount: String) {
        this.customerId = customerId
        etInvoiceNo.setText(invoiceNumber)
        etInvoiceDate.setText(invoiceDate.split(" ")[0])
        etCustomerName.setText(customerName)
        etCustomerAddress.setText(customerAddress)
        etPhoneNo.setText(customerPhone)

        this.discountAmount = MathUtils.toDouble(discountAmount)
        this.taxAmount = MathUtils.toDouble(taxAmount)
        this.netTotal = MathUtils.toDouble(netAmount)
        this.total = MathUtils.toDouble(grossAmount)

        cbCredit.isChecked = customerId != "1"
        tvValueNetTotal.text = netAmount
        tvValueTaxAmount.text = taxAmount
        tvValueDiscount.text = discountAmount
        tvValueTotal.text = grossAmount

        isRecyclerViewEnabled = false
        rv.adapter = SalesOrderRvAdapter(
                this@SalesOrderFragment,
                productItemsList,
                (mContext as SalesOrderActivity).productList,
                (mContext as SalesOrderActivity).productListNames,
                tvItemCount,
                etInvoiceNo,
                etCustomerName,
                etCustomerAddress,
                etPhoneNo,
                cbCredit)

        fabFinish.isEnabled = true
        isViewsEnabled(false)

        fabFinish.setOnClickListener {
            if (isRecyclerViewEnabled) { // EDITABLE
                if (!isValidAll((mContext as SalesOrderActivity).customerList))
                    return@setOnClickListener

                if (productItemsList.size == 0) {
                    SnackBarUtils.showSnackBar(mContext as Activity, "Please add products")
                    return@setOnClickListener
                }

                val intent = Intent(mContext, SalesOrderBillActivity::class.java)
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
                intent.putExtra("KEY_TOTAL", ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(this.total))
                intent.putExtra("KEY_TAX_AMOUNT", ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(this.taxAmount))
                intent.putExtra("KEY_DISCOUNT_AMOUNT", ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(this.discountAmount))
                intent.putExtra("KEY_NET_TOTAL", ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(this.netTotal))
                intent.putExtra("KEY_SPECIAL_DISCOUNT_AMOUNT", specialDiscount)
                startActivity(intent)
            } else {
                fabFinish.setImageResource(R.drawable.ic_finish)
                isViewsEnabled(true)
                isRecyclerViewEnabled = true
                rv.adapter.notifyDataSetChanged()
            }
        }

        etCustomerName.threshold = 1
        etCustomerName.setAdapter(ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, (mContext as SalesOrderActivity).customerListNames))
        etCustomerName.setOnItemClickListener { adapterView, v, i1, l ->
            // where the position i1 is invalid so this technique was used
            val vl = adapterView.getItemAtPosition(i1) as String
            val position = (mContext as SalesOrderActivity).customerListNames.indexOf(vl)

            Tools.hideSoftKeyboard(mContext as Activity)

            this.customerId = (mContext as SalesOrderActivity).customerList[position].customerId
            etCustomerAddress.setText((mContext as SalesOrderActivity).customerList[position].address1)
            etPhoneNo.setText((mContext as SalesOrderActivity).customerList[position].telephone)
            ratePlan = (mContext as SalesOrderActivity).customerList[position].ratePlan.toInt()
            cbCredit.isChecked = this.customerId != "1"

            for (iL in 0 until productItemsList.size) {
                val itemPos = (mContext as SalesOrderActivity).productList.indexOfFirst { it -> it.productCode == productItemsList[iL].productCode }
                if (itemPos == -1)
                    continue
                if (productItemsList[iL].rate1 == (mContext as SalesActivity).productList[itemPos].rate1 ||
                        productItemsList[iL].rate1 == (mContext as SalesActivity).productList[itemPos].rate2 ||
                        productItemsList[iL].rate1 == (mContext as SalesActivity).productList[itemPos].rate3) {
                    productItemsList[iL].rate1 = when (ratePlan) {
                        0 -> (mContext as SalesOrderActivity).productList[itemPos].rate1
                        1 -> (mContext as SalesOrderActivity).productList[itemPos].rate2
                        else -> (mContext as SalesOrderActivity).productList[itemPos].rate3
                    }
                    calculateAmounts(iL, productItemsList[iL].rate1, productItemsList[iL].discountPercentage, productItemsList[iL].sGSTPercentage, productItemsList[iL].cGSTPercentage, productItemsList[iL].quantity)
                }
            }
            rv.adapter.notifyDataSetChanged()
            calculateTotal()
        }

        ivRead.setOnClickListener {
            if (!isValidAll((mContext as SalesOrderActivity).customerList))
                return@setOnClickListener

            val cameraPermission = arrayOf(android.Manifest.permission.CAMERA)
            if (PermissionUtils.hasAllPermissions(mContext!!, cameraPermission)) {
                startRead()
            } else {
                // FOR FRAGMENT USE requestPermissions function NOT ActivityCompat.requestPermissions
                requestPermissions(cameraPermission, REQUEST_CODE_PERMISSION_CAMERA)
            }
        }

        fabAddItem.setOnClickListener {
            if (!isValidAll((mContext as SalesOrderActivity).customerList))
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

    private fun isViewsEnabled(b: Boolean) {
        etCustomerName.isEnabled = b
        etCustomerAddress.isEnabled = b
        etPhoneNo.isEnabled = b
        fabAddItem.isEnabled = b
        cbCredit.isEnabled = b
        ivRead.isEnabled = b
    }

    private fun isValidAll(customers: ArrayList<CustomerModel>): Boolean {
        val customerName = etCustomerName.text.toString().trim()
        val customerAddress = etCustomerAddress.text.toString().trim()
        val customerPhone = etPhoneNo.text.toString().trim()
        val invoiceDate = etInvoiceDate.text.toString().trim()

        if (TextUtils.isEmpty(invoiceDate)) {
            SnackBarUtils.showSnackBar(mContext as Activity, "Please choose a valid date")
            return false
        }

        if (cbCredit.isChecked) {
            if (!customers.any { it1 -> it1.name.trim() == customerName && it1.address1.trim() == customerAddress && it1.telephone.trim() == customerPhone && it1.customerId == customerId }) {
                SnackBarUtils.showSnackBar(mContext as Activity, "Please choose a valid customer")
                return false
            }

            if (customerId == "1") {
                SnackBarUtils.showSnackBar(mContext as Activity, "Selected customer is not a credit customer")
                return false
            }
        }

        if (productItemsList.size > 0) {
            for (i in 0 until productItemsList.size) {
                if (!(mContext as SalesOrderActivity).productList.any { it2 -> it2.displayName == productItemsList[i].displayName }) {
                    SnackBarUtils.showSnackBar(mContext as Activity, "Invalid product list")
                    return false
                }
            }
        }
        return true
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
        tvValueTotal.text = ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(total)
        tvValueTaxAmount.text = ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(taxAmount)
        tvValueDiscount.text = ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(discountAmount)
        tvValueNetTotal.text = ("%." + DataCollections.getInstance(mContext!!).getUser()!!.amountType + "f").format(netTotal)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_CAMERA -> {
                if (PermissionUtils.hasAllPermissionsGranted(grantResults)) {
                    startRead()
                } else {
                    SnackBarUtils.showSnackBar(mContext as Activity, "Camera access denied !")
                }
            }
        }
    }

    private fun startRead() {
        val v = LayoutInflater.from(mContext).inflate(R.layout.test_layout_dialog_reading, null)

        val dialog = Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(v)
        dialog.show()

        val surfaceView = v.findViewById<BarcodeScanningView>(R.id.surfaceView)
        val ivFrame = v.findViewById<ImageView>(R.id.ivFrame)
        surfaceView.setFrame(ivFrame)

        surfaceView.setCallBack(object : BarcodeScanningView.ReceiverCallBack {
            override fun onReceiveData(qrValue: String, barcodeDetector: BarcodeDetector, barcodeProcessor: BarcodeScanningView.BarcodeProcessor) {
                barcodeDetector.release()
                MediaPlayer.create(mContext, R.raw.beep1).start()
                if (TextUtils.isEmpty(qrValue)) {
                    (mContext as Activity).runOnUiThread {
                        Snackbar.make(v, "Invalid Barcode", Snackbar.LENGTH_SHORT).show()
                        Handler().postDelayed({
                            barcodeDetector.setProcessor(barcodeProcessor)
                        }, DELAY_BARCODE_RESCAN)
                    }
                } else {
                    val isMatchProductCode = isMatch((mContext as SalesOrderActivity).productList, qrValue)
                    if (isMatchProductCode != null) {
                        productItemsList.add(isMatchProductCode)
                        (mContext as Activity).runOnUiThread {
                            rv.adapter.notifyDataSetChanged()
                            rv.scrollToPosition(productItemsList.size - 1)
                        }
                        dialog.cancel()
                    } else {
                        (mContext as Activity).runOnUiThread {
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

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callGetInvoiceDetails)
    }

    private fun init(view: View) {
        layoutParent = view.findViewById(R.id.layoutParent)
        rv = view.findViewById(R.id.rv)
        cbCredit = view.findViewById(R.id.cbCredit)
        tvItemCount = view.findViewById(R.id.tvItemCount)
        ivRead = view.findViewById(R.id.ivRead)
        ivSearch = view.findViewById(R.id.ivSearch)
        fabFinish = view.findViewById(R.id.fabFinish)
        etInvoiceNo = view.findViewById(R.id.etInvoiceNo)
        etInvoiceDate = view.findViewById(R.id.etInvoiceDate)
        etCustomerName = view.findViewById(R.id.etCustomerName)
        etCustomerAddress = view.findViewById(R.id.etCustomerAddress)
        etPhoneNo = view.findViewById(R.id.etPhoneNo)
        fabAddItem = view.findViewById(R.id.fabAddItem)
        tvValueTotal = view.findViewById(R.id.tvValueTotal)
        tvValueTaxAmount = view.findViewById(R.id.tvValueTaxAmount)
        tvValueDiscount = view.findViewById(R.id.tvValueDiscount)
        tvValueNetTotal = view.findViewById(R.id.tvValueNetTotal)
        pbLoadingInvoiceDetails = view.findViewById(R.id.pbLoadingInvoiceDetails)
        tvMessage = view.findViewById(R.id.tvMessage)
        tvRetry = view.findViewById(R.id.tvRetry)
    }
}