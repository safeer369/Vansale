package com.teamayka.vansaleandmgmt.ui.filedforce.activities

import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.firebase.FireBaseNotificationBroadcastReceiver
import com.teamayka.vansaleandmgmt.ui.filedforce.adapters.RvFailedListAdapter
import com.teamayka.vansaleandmgmt.ui.filedforce.models.FieldForceTypes
import com.teamayka.vansaleandmgmt.ui.filedforce.models.RvListModel
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import com.teamayka.vansaleandmgmt.utils.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class FailedActivity : AppCompatActivity() {

    private lateinit var rvList: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvRetry: TextView
    private lateinit var tvMessage: TextView
    private lateinit var ivMap: ImageView
    private lateinit var ivBack: ImageView
    private lateinit var ivRefresh: ImageView
    private lateinit var ivNotification: ImageView

    private lateinit var notificationReceiver: FireBaseNotificationBroadcastReceiver

    private var callGetList: Call? = null
    private var callRefresh: Call? = null

    var date = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failed)
        init()

        ivBack.setOnClickListener {
            super.onBackPressed()
        }

        date = intent.getStringExtra("KEY_DATE")
        rvList.addItemDecoration(RvItemMargin(10))

        getReports()

        notificationReceiver = FireBaseNotificationBroadcastReceiver(ivNotification)
        registerReceiver(notificationReceiver, IntentFilter(Constants.INTENT_FILTER_RECEIVE_NOTIFICATION))
    }

    inner class TouchHelper(val adapter: RvFailedListAdapter) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
        private var vh: RvFailedListAdapter.ViewHolder? = null
        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (viewHolder is RvFailedListAdapter.ViewHolder) {
                vh = viewHolder
                viewHolder.layoutParent.setBackgroundColor(if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) Color.parseColor("#dbdbdb") else {
                    Handler().postDelayed({
                        try {
                            adapter.notifyDataSetChanged()
                            if (rvList.adapter is RvFailedListAdapter) {
                                val list = (rvList.adapter as RvFailedListAdapter).list
                                DataCollections.getInstance(this@FailedActivity).updateFailedList(list, date)
                            }
                        } catch (e: Exception) {
                        }
                    }, 500)
                    Color.WHITE
                })
            } else {
                vh?.layoutParent?.setBackgroundColor(if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) Color.parseColor("#dbdbdb") else {
                    Handler().postDelayed({
                        try {
                            adapter.notifyDataSetChanged()
                            if (rvList.adapter is RvFailedListAdapter) {
                                val list = (rvList.adapter as RvFailedListAdapter).list
                                DataCollections.getInstance(this@FailedActivity).updateFailedList(list, date)
                            }
                        } catch (e: Exception) {
                        }
                    }, 500)
                    Color.WHITE
                })
            }
        }

        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            adapter.swapItem(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//            adapter.removeItem(viewHolder.adapterPosition)
        }
    }

    private fun getReports() {
        rvList.visibility = View.GONE
        tvMessage.visibility = View.GONE
        tvRetry.visibility = View.GONE
        ivMap.visibility = View.GONE
        ivRefresh.visibility = View.GONE
        pbLoading.visibility = View.VISIBLE

        tvRetry.setOnClickListener {
            getReports()
        }

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("Date", date)
                .add("Type", FieldForceTypes.FAILED)
                .build()

        Log.e("__________post", "_field_force_failed: ")

        val request = Request.Builder()
                .url(PublicUrls.URL_GET_FIELD_FORCE_LIST)
                .post(body)
                .build()

        callGetList = client.newCall(request)
        callGetList?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    rvList.visibility = View.GONE
                    pbLoading.visibility = View.GONE
                    ivMap.visibility = View.GONE
                    ivRefresh.visibility = View.GONE
                    tvRetry.visibility = View.VISIBLE
                    tvMessage.visibility = View.VISIBLE
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
                Log.e("_____________resp", "_field_force_failed : $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val jaTicketDetails = jo.getJSONArray("TicketDetails")
                        val list = java.util.ArrayList<RvListModel>()
                        for (i in 0 until jaTicketDetails.length()) {
                            val joTicketDetails = jaTicketDetails.getJSONObject(i)
                            val ticketIDNo = joTicketDetails.getString("TicketIDNo")
                            val ticketId = joTicketDetails.getString("TicketID")
                            val serviceType = joTicketDetails.getString("ServiceType")
                            val serviceName = joTicketDetails.getString("ServiceName")
                            val customer = joTicketDetails.getString("Customer")
                            val phone1 = joTicketDetails.getString("Phone1")
                            val phone2 = joTicketDetails.getString("Phone2")
                            val address = joTicketDetails.getString("Address")
                            val ticketDescription = joTicketDetails.getString("TicketDescription")
                            val orderInfo = joTicketDetails.getString("OrderInfo")
                            val longitude = joTicketDetails.getString("Longitude")
                            val latitude = joTicketDetails.getString("Latitude")
                            val statusId = joTicketDetails.getString("StatusID")
                            val remarks = joTicketDetails.getString("Remarks")
                            val receivedBy = joTicketDetails.getString("ReceivedBy")
                            var time = ""
                            if (joTicketDetails.has("DeliveryTime"))
                                time = joTicketDetails.getString("DeliveryTime")
                            list.add(RvListModel(ticketIDNo, ticketId, statusId, serviceType, serviceName, customer, phone1, phone2, address, ticketDescription, orderInfo, longitude, latitude, remarks, receivedBy, time))
                        }

                        if (list.size <= 0) {
                            runOnUiThread {
                                rvList.visibility = View.GONE
                                ivMap.visibility = View.GONE
                                ivRefresh.visibility = View.GONE
                                tvMessage.visibility = View.VISIBLE
                                tvRetry.visibility = View.VISIBLE
                                pbLoading.visibility = View.GONE

                                tvMessage.text = getString(R.string.empty_list)
                            }
                        } else {
                            runOnUiThread {
                                tvMessage.visibility = View.GONE
                                tvRetry.visibility = View.GONE
                                pbLoading.visibility = View.GONE
                                rvList.visibility = View.VISIBLE
                                ivMap.visibility = View.VISIBLE
                                ivRefresh.visibility = View.VISIBLE

                                showList(list)
                            }
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            rvList.visibility = View.GONE
                            ivMap.visibility = View.GONE
                            ivRefresh.visibility = View.GONE
                            tvMessage.visibility = View.VISIBLE
                            tvRetry.visibility = View.VISIBLE
                            pbLoading.visibility = View.GONE
                            tvMessage.text = message
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_______exc", "_field_force_failed : " + e.message)
                    runOnUiThread {
                        rvList.visibility = View.GONE
                        ivMap.visibility = View.GONE
                        ivRefresh.visibility = View.GONE
                        tvMessage.visibility = View.VISIBLE
                        tvRetry.visibility = View.VISIBLE
                        pbLoading.visibility = View.GONE
                        tvMessage.text = "$resp"
                    }
                }
            }
        })
    }

    private fun refresh(latitude: String, longitude: String) {
        rvList.visibility = View.GONE
        tvMessage.visibility = View.GONE
        tvRetry.visibility = View.GONE
        ivMap.visibility = View.GONE
        ivRefresh.visibility = View.GONE
        pbLoading.visibility = View.VISIBLE

        tvRetry.setOnClickListener {
            refresh(latitude, longitude)
        }

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("Date", date)
                .add("Type", FieldForceTypes.FAILED)
                .add("Latitude", latitude)
                .add("Longitude", longitude)
                .build()

        Log.e("__________post", "_field_force_failed : " +
                "Date : $date" +
                "Type : ${FieldForceTypes.FAILED}" +
                "Latitude : $latitude" +
                "Longitude : $longitude")

        val request = Request.Builder()
                .url(PublicUrls.URL_FIELD_FORCE_REFRESH)
                .post(body)
                .build()

        callRefresh = client.newCall(request)
        callRefresh?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    rvList.visibility = View.GONE
                    pbLoading.visibility = View.GONE
                    ivMap.visibility = View.GONE
                    ivRefresh.visibility = View.GONE
                    tvRetry.visibility = View.VISIBLE
                    tvMessage.visibility = View.VISIBLE
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
                Log.e("_____________resp", "_field_force_failed : $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val jaTicketDetails = jo.getJSONArray("RoutedList")
                        val list = java.util.ArrayList<RvListModel>()
                        for (i in 0 until jaTicketDetails.length()) {
                            val joTicketDetails = jaTicketDetails.getJSONObject(i)
                            val ticketIDNo = joTicketDetails.getString("TicketIDNo")
                            val ticketId = joTicketDetails.getString("TicketID")
                            val serviceType = joTicketDetails.getString("ServiceType")
                            val serviceName = joTicketDetails.getString("ServiceName")
                            val customer = joTicketDetails.getString("Customer")
                            val phone1 = joTicketDetails.getString("Phone1")
                            val phone2 = joTicketDetails.getString("Phone2")
                            val address = joTicketDetails.getString("Address")
                            val ticketDescription = joTicketDetails.getString("TicketDescription")
                            val orderInfo = joTicketDetails.getString("OrderInfo")
                            val longitudeNew = joTicketDetails.getString("Longitude")
                            val latitudeNew = joTicketDetails.getString("Latitude")
                            val statusId = joTicketDetails.getString("StatusID")
                            val remarks = joTicketDetails.getString("Remarks")
                            val receivedBy = joTicketDetails.getString("ReceivedBy")
                            var time = ""
                            if (joTicketDetails.has("DeliveryTime"))
                                time = joTicketDetails.getString("DeliveryTime")
                            list.add(RvListModel(ticketIDNo, ticketId, statusId, serviceType, serviceName, customer, phone1, phone2, address, ticketDescription, orderInfo, longitudeNew, latitudeNew, remarks, receivedBy, time))
                        }

                        if (list.size <= 0) {
                            runOnUiThread {
                                rvList.visibility = View.GONE
                                ivMap.visibility = View.GONE
                                ivRefresh.visibility = View.GONE
                                tvMessage.visibility = View.VISIBLE
                                tvRetry.visibility = View.VISIBLE
                                pbLoading.visibility = View.GONE

                                tvMessage.text = getString(R.string.empty_list)
                            }
                        } else {
                            runOnUiThread {
                                tvMessage.visibility = View.GONE
                                tvRetry.visibility = View.GONE
                                pbLoading.visibility = View.GONE
                                rvList.visibility = View.VISIBLE
                                ivMap.visibility = View.VISIBLE
                                ivRefresh.visibility = View.VISIBLE

                                showList(list)
                            }
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            rvList.visibility = View.GONE
                            ivMap.visibility = View.GONE
                            ivRefresh.visibility = View.GONE
                            tvMessage.visibility = View.VISIBLE
                            tvRetry.visibility = View.VISIBLE
                            pbLoading.visibility = View.GONE
                            tvMessage.text = message
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_______exc", "_field_force_failed: " + e.message)
                    runOnUiThread {
                        rvList.visibility = View.GONE
                        ivMap.visibility = View.GONE
                        ivRefresh.visibility = View.GONE
                        tvMessage.visibility = View.VISIBLE
                        tvRetry.visibility = View.VISIBLE
                        pbLoading.visibility = View.GONE
                        tvMessage.text = "$resp"
                    }
                }
            }
        })
    }

    private fun showList(list: ArrayList<RvListModel>) {
        var previousList = ArrayList<RvListModel>()
        if (date == CalendarUtils.getCurrentDate())
            previousList = DataCollections.getInstance(this).getFailedList()

        for (i in 0 until list.size) {
            if (!previousList.any { it.ticketId == list[i].ticketId }) {
                previousList.add(list[i])
            }
        }

        if (rvList.adapter is RvFailedListAdapter) {
            (rvList.adapter as RvFailedListAdapter).updateList(previousList)
            DataCollections.getInstance(this).updateFailedList(list, date)
        } else {
            rvList.adapter = RvFailedListAdapter(previousList, date)
            val callBack = TouchHelper(rvList.adapter as RvFailedListAdapter)
            val helper = ItemTouchHelper(callBack)
            helper.attachToRecyclerView(rvList)
        }

        ivMap.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putParcelableArrayListExtra("KEY_LIST", (rvList.adapter as RvFailedListAdapter).list)
            intent.putExtra("KEY_DATE", date)
            intent.putExtra("KEY_IS_FAILED", true)
            startActivity(intent)
        }

        ivRefresh.setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage("Your arranged list will be lost. Confirm?")
                    .setPositiveButton("OPTIMISE ROUTE", DialogInterface.OnClickListener { p0, p1 ->
                        val location = PreferenceManager.getDefaultSharedPreferences(this@FailedActivity).getString("KEY_CURRENT_LOCATION", "")
                        if (TextUtils.isEmpty(location)) {
                            SnackBarUtils.showSnackBar(this@FailedActivity, "Can't get location")
                            return@OnClickListener
                        } else {
                            val latitude = location.split(",")[0]
                            val longitude = location.split(",")[1]
                            refresh(latitude, longitude)
                        }
                    })
                    .setNegativeButton("CANCEL") { p0, p1 -> }
                    .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callGetList)
        OkHttpUtils.cancelCalls(callRefresh)
        unregisterReceiver(notificationReceiver)
    }

    private fun init() {
        rvList = findViewById(R.id.rvList)
        pbLoading = findViewById(R.id.pbLoading)
        tvRetry = findViewById(R.id.tvRetry)
        tvMessage = findViewById(R.id.tvMessage)
        ivMap = findViewById(R.id.ivMap)
        ivBack = findViewById(R.id.ivBack)
        ivRefresh = findViewById(R.id.ivRefresh)
        ivNotification = findViewById(R.id.ivNotification)
    }
}
