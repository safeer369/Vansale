package com.teamayka.vansaleandmgmt.ui.filedforce.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.adapters.SrAdapter
import com.teamayka.vansaleandmgmt.ui.main.models.CustomerModel
import com.teamayka.vansaleandmgmt.utils.DataCollections
import com.teamayka.vansaleandmgmt.utils.OkHttpUtils
import com.teamayka.vansaleandmgmt.utils.SnackBarUtils
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class ViewTicketActivity : AppCompatActivity() {

    private lateinit var tvCustomerName: TextView
    private lateinit var tvTicketIdNo: TextView
    private lateinit var tvPhone1: TextView
    private lateinit var tvPhone2: TextView
    private lateinit var tvValueAddress: TextView
    private lateinit var tvValueTicketDescription: TextView
    private lateinit var tvValueOrderInfo: TextView
    private lateinit var tvValueRemarks: TextView
    private lateinit var tvValueReceivedBy: TextView
    private lateinit var sValueStatus: Spinner
    private lateinit var tvSave: TextView
    private lateinit var pbLoadingSave: com.teamayka.vansaleandmgmt.components.ProgressBar
    private lateinit var pbLoadingStatus: ProgressBar
    private lateinit var ivMap: ImageView
    private lateinit var ivBack: ImageView
    private lateinit var ivImage: ImageView
    private lateinit var ivSignature: ImageView
    private lateinit var ivSignatureHint: ImageView

    private var callUpdateTicket: Call? = null
    private var callGetStatus: Call? = null

    private var isEnableUpdateStatus = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_ticket)
        init()

        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_SIGNATURE", "").apply()
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_IMAGE", "").apply()

        ivBack.setOnClickListener {
            super.onBackPressed()
        }

        val date = intent.getStringExtra("KEY_DATE")
        val isCompleted = intent.getBooleanExtra("KEY_IS_COMPLETED", false)
        val isFailed = intent.getBooleanExtra("KEY_IS_FAILED", false)

        val df = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val formattedDate = df.parse(date)

        val currentDate = Calendar.getInstance().time
        val formattedCurrentDate = df.parse(df.format(currentDate))

        if (formattedDate != formattedCurrentDate) {
            ivImage.isEnabled = false
            ivSignature.isEnabled = false
            tvValueRemarks.isEnabled = false
            tvValueReceivedBy.isEnabled = false
            tvSave.visibility = View.GONE
            isEnableUpdateStatus = false
        }

        if (isCompleted) {
            ivImage.isEnabled = false
            ivSignature.isEnabled = false
            tvValueRemarks.isEnabled = false
            tvValueReceivedBy.isEnabled = false
            tvSave.visibility = View.GONE
            isEnableUpdateStatus = false
        }

        val customerName = intent.getStringExtra("KEY_CUSTOMER_NAME")
        val phone1 = intent.getStringExtra("KEY_PHONE_1")
        val phone2 = intent.getStringExtra("KEY_PHONE_2")
        val address = intent.getStringExtra("KEY_ADDRESS")
        val ticketDescription = intent.getStringExtra("KEY_TICKET_DESCRIPTION")
        val orderInfo = intent.getStringExtra("KEY_ORDER_INFO")
        val ticketId = intent.getStringExtra("KEY_TICKET_ID")
        val ticketIdNo = intent.getStringExtra("KEY_TICKET_ID_NO")
        val statusId = intent.getStringExtra("KEY_STATUS_ID")
        val latitude = intent.getStringExtra("KEY_LATITUDE")
        val longitude = intent.getStringExtra("KEY_LONGITUDE")
        val remarks = intent.getStringExtra("KEY_REMARKS")
        val receivedBy = intent.getStringExtra("KEY_RECEIVED_BY")

        tvTicketIdNo.text = ticketIdNo
        tvCustomerName.text = customerName
        tvPhone1.text = phone1
        tvPhone2.text = phone2
        tvValueAddress.text = address
        tvValueTicketDescription.text = ticketDescription
        tvValueOrderInfo.text = orderInfo
        tvValueRemarks.text = remarks
        tvValueReceivedBy.text = receivedBy

        getStatus(statusId)
        sValueStatus.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                if (sValueStatus.adapter == null || (sValueStatus.adapter != null && sValueStatus.adapter.count == 0))
                    getStatus(statusId)
            }
            !isEnableUpdateStatus // prevent user from changing item
        }

        tvPhone1.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone1")
            startActivity(intent)
        }

        tvPhone2.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone2")
            startActivity(intent)
        }

        ivMap.setOnClickListener {
            val location = PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_CURRENT_LOCATION", "")
            if (TextUtils.isEmpty(location)) {
                SnackBarUtils.showSnackBar(this, "Your location not found")
            } else {
                val intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=$location&daddr=$latitude,$longitude"))
                startActivity(intent)
            }
        }

        ivImage.setOnClickListener {
            showImage()
        }

        ivSignature.setOnClickListener {
            showSignature()
        }

        tvSave.setOnClickListener {
            val remarksNew = tvValueRemarks.text.toString().trim()
            val receivedByNew = tvValueReceivedBy.text.toString().trim()
            val listStatuses = (sValueStatus.adapter as SrAdapter).itemList
            val sId = listStatuses[sValueStatus.selectedItemPosition].customerId

            if (TextUtils.isEmpty(receivedByNew) && isFailed && sId == "3") {
                SnackBarUtils.showSnackBar(this, "please enter receiver")
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(receivedByNew) && !isFailed && !isCompleted && sId == "3") { //inProgress
                SnackBarUtils.showSnackBar(this, "please enter receiver")
                return@setOnClickListener
            }

            if (sValueStatus.adapter == null) {
                SnackBarUtils.showSnackBar(this, "No statuses")
                return@setOnClickListener
            }

            if (listStatuses.size == 0) {
                SnackBarUtils.showSnackBar(this, "No statuses")
                return@setOnClickListener
            }
            if (statusId != sId) {
                AlertDialog.Builder(this)
                        .setMessage("Confirm change status !")
                        .setPositiveButton("SAVE") { p0, p1 -> uploadData(ticketId, remarksNew, statusId, sId, receivedByNew, isCompleted, isFailed) }
                        .setNegativeButton("CANCEL") { p0, p1 -> }
                        .show()
            } else
                uploadData(ticketId, remarksNew, statusId, sId, receivedByNew, isCompleted, isFailed)
        }
    }

    private fun showSignature() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_SIGNATURE", "").apply()
        val intent = Intent(this, SignatureActivity::class.java)
        startActivity(intent)
    }

    private fun showImage() {
        val image = PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_IMAGE", "")
        if (TextUtils.isEmpty(image)) {
            val intent = Intent(this, ImageCaptureActivity::class.java)
            startActivity(intent)
        } else {
            val tempImagePath = cacheDir.absolutePath + File.separator + "temp_img_" + packageName + "_12345"
            val imageFile = File("$tempImagePath(1).jpg")
            if (imageFile.exists()) {
                val intent = Intent(this, CapturedImageViewActivity::class.java)
                intent.putExtra("KEY_IMAGE_PATH", imageFile.absolutePath)
                startActivity(intent)
            } else {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_IMAGE", "").apply()
                Toast.makeText(this@ViewTicketActivity, "Image not exists", Toast.LENGTH_LONG).show()
                val intent = Intent(this, ImageCaptureActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun getStatus(statusId: String) {
        pbLoadingStatus.visibility = View.VISIBLE
        sValueStatus.isEnabled = false

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")

        val request = Request.Builder()
                .url(PublicUrls.URL_GET_FIELD_FORCE_STATUS)
                .post(body.build())
                .build()

        callGetStatus = client.newCall(request)
        callGetStatus?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("_____________error", " get status : ${e?.message}")
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoadingStatus.visibility = View.GONE
                    sValueStatus.isEnabled = true
                    if (e is UnknownHostException)
                        SnackBarUtils.showSnackBar(this@ViewTicketActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@ViewTicketActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp", "get status  $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val jaData = jo.getJSONArray("Data")
                        val listStatuses = ArrayList<CustomerModel>()
                        for (i in 0 until jaData.length()) {
                            val joData = jaData.getJSONObject(i)
                            val sId = joData.getString("StatusID")
                            val status = joData.getString("Status")
                            listStatuses.add(CustomerModel(sId, "", status, "", "", "", ""))
                        }

                        runOnUiThread {
                            pbLoadingStatus.visibility = View.GONE
                            sValueStatus.isEnabled = true

                            sValueStatus.adapter = SrAdapter(this@ViewTicketActivity, listStatuses)
                            val index = getSelection(statusId, listStatuses)
                            if (index != -1)
                                sValueStatus.setSelection(index)
                        }

                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoadingStatus.visibility = View.GONE
                            sValueStatus.isEnabled = true
                            SnackBarUtils.showSnackBar(this@ViewTicketActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc", "get status" + e.message)
                    runOnUiThread {
                        pbLoadingStatus.visibility = View.GONE
                        sValueStatus.isEnabled = true
                        SnackBarUtils.showSnackBar(this@ViewTicketActivity, "$resp")
                    }
                }
            }
        })
    }

    private fun getSelection(statusId: String, listStatuses: ArrayList<CustomerModel>): Int {
        for (i in 0 until listStatuses.size) {
            if (listStatuses[i].customerId == statusId)
                return i
        }
        return -1
    }

    private fun uploadData(ticketId: String, remarks: String, stausId: String, sId: String, receivedBy: String, completed: Boolean, failed: Boolean) {
        pbLoadingSave.visibility = View.VISIBLE
        tvSave.isEnabled = false

        val client = OkHttpUtils.getOkHttpClient()

        val image = PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_IMAGE", "")
        val signature = PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_SIGNATURE", "")

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("TicketID", ticketId)
                .add("StatusID", sId)
                .add("UserRemarks", remarks)
                .add("ReceivedBy", receivedBy)
                .add("ImageFile", image)
                .add("Signature", signature)

        Log.e("_______________", "save ticket ::  ticketId : $ticketId, statusId : $sId, remarks : $remarks, receivedBy : $receivedBy, image : $image, signature : $signature")

        val request = Request.Builder()
                .url(PublicUrls.URL_UPDATE_FIELD_FORCE_STATUS)
                .post(body.build())
                .build()

        callUpdateTicket = client.newCall(request)
        callUpdateTicket?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("_____________error", " save ticket : ${e?.message}")
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoadingSave.visibility = View.GONE
                    tvSave.isEnabled = true
                    if (e is UnknownHostException)
                        SnackBarUtils.showSnackBar(this@ViewTicketActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@ViewTicketActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp", "save ticket  $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val message = jo.getString("Message")

                        runOnUiThread {
                            pbLoadingSave.visibility = View.GONE
                            tvSave.isEnabled = true
                            Toast.makeText(this@ViewTicketActivity, message, Toast.LENGTH_SHORT).show()
                            PreferenceManager.getDefaultSharedPreferences(this@ViewTicketActivity).edit().putString("KEY_IMAGE", "").apply()
                            PreferenceManager.getDefaultSharedPreferences(this@ViewTicketActivity).edit().putString("KEY_SIGNATURE", "").apply()
                            if (failed) {
                                DataCollections.getInstance(this@ViewTicketActivity).deleteItemFromFailedList(ticketId)
                            } else if (!failed && !completed) { // if inProgress (view ticket not allowed for allocated)
                                DataCollections.getInstance(this@ViewTicketActivity).deleteItemFromInProgress(ticketId)
                            }
                            finishAffinity()
                        }

                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoadingSave.visibility = View.GONE
                            tvSave.isEnabled = true
                            SnackBarUtils.showSnackBar(this@ViewTicketActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc", "save ticket" + e.message)
                    runOnUiThread {
                        pbLoadingSave.visibility = View.GONE
                        tvSave.isEnabled = true
                        SnackBarUtils.showSnackBar(this@ViewTicketActivity, "$resp")
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val signature = PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_SIGNATURE", "")
        if (!TextUtils.isEmpty(signature))
            ivSignatureHint.visibility = View.VISIBLE
        else
            ivSignatureHint.visibility = View.GONE

    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callUpdateTicket)
        OkHttpUtils.cancelCalls(callGetStatus)
    }

    private fun init() {
        tvCustomerName = findViewById(R.id.tvCustomerName)
        tvTicketIdNo = findViewById(R.id.tvTicketIdNo)
        tvPhone1 = findViewById(R.id.tvPhone1)
        tvPhone2 = findViewById(R.id.tvPhone2)
        tvValueAddress = findViewById(R.id.tvValueAddress)
        tvValueTicketDescription = findViewById(R.id.tvValueTicketDescription)
        tvValueOrderInfo = findViewById(R.id.tvValueOrderInfo)
        tvValueRemarks = findViewById(R.id.tvValueRemarks)
        tvValueReceivedBy = findViewById(R.id.tvValueReceivedBy)
        sValueStatus = findViewById(R.id.sValueStatus)
        tvSave = findViewById(R.id.tvSave)
        pbLoadingSave = findViewById(R.id.pbLoadingSave)
        pbLoadingStatus = findViewById(R.id.pbLoadingStatus)
        ivMap = findViewById(R.id.ivMap)
        ivBack = findViewById(R.id.ivBack)
        ivImage = findViewById(R.id.ivImage)
        ivSignature = findViewById(R.id.ivSignature)
        ivSignatureHint = findViewById(R.id.ivSignatureHint)
    }
}
