package com.teamayka.vansaleandmgmt.ui.filedforce.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.filedforce.adapters.RvCheckInListAdapter
import com.teamayka.vansaleandmgmt.ui.filedforce.models.RvCheckInListModel
import com.teamayka.vansaleandmgmt.utils.DataCollections
import com.teamayka.vansaleandmgmt.utils.OkHttpUtils
import com.teamayka.vansaleandmgmt.utils.RvItemMargin
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class CheckInListActivity : AppCompatActivity() {

    private lateinit var rvList: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvRetry: TextView
    private lateinit var tvMessage: TextView
    private lateinit var ivBack: ImageView

    private var callGetList: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in_list)
        init()

        ivBack.setOnClickListener {
            super.onBackPressed()
        }

        val date = intent.getStringExtra("KEY_DATE")

        rvList.addItemDecoration(RvItemMargin(10))
        getReports(date)
    }

    private fun getReports(date: String) {
        rvList.visibility = View.GONE
        tvMessage.visibility = View.GONE
        tvRetry.visibility = View.GONE
        pbLoading.visibility = View.VISIBLE

        tvRetry.setOnClickListener {
            getReports(date)
        }

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("Date", date)
                .build()

        Log.e("__________post", "_field_force_check_in_list : ")

        val request = Request.Builder()
                .url(PublicUrls.URL_FIELD_FORCE_GET_CHECK_IN_LIST)
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
                Log.e("_____________resp", "_field_force_check_in_list  : $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val jaTicketDetails = jo.getJSONArray("UnScheduledTaskDetails")
                        val list = java.util.ArrayList<RvCheckInListModel>()
                        for (i in 0 until jaTicketDetails.length()) {
                            val joTicketDetails = jaTicketDetails.getJSONObject(i)
                            val ticketIDNo = joTicketDetails.getString("TicketIDNo")
                            val ticketId = joTicketDetails.getString("TicketID")
                            val serviceType = joTicketDetails.getString("ServiceType")
                            val serviceName = joTicketDetails.getString("ServiceName")
                            val customer = joTicketDetails.getString("Customer")
                            val contactPerson = joTicketDetails.getString("ContactPerson")
                            val phone1 = joTicketDetails.getString("Phone1")
                            val phone2 = joTicketDetails.getString("Phone2")
                            val address = joTicketDetails.getString("Address")
                            val ticketDescription = joTicketDetails.getString("TicketDescription")
                            val orderInfo = joTicketDetails.getString("OrderInfo")
                            val longitude = joTicketDetails.getString("Longitude")
                            val latitude = joTicketDetails.getString("Latitude")
                            val imageUrl = joTicketDetails.getString("ImageUrl")
                            list.add(RvCheckInListModel(ticketIDNo, ticketId, serviceType, serviceName, customer, phone1, phone2, address, ticketDescription, orderInfo, longitude, latitude, imageUrl, contactPerson))// todo change
                        }

                        if (list.size <= 0) {
                            runOnUiThread {
                                rvList.visibility = View.GONE
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

                                showList(list)
                            }
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            rvList.visibility = View.GONE
                            tvMessage.visibility = View.VISIBLE
                            tvRetry.visibility = View.VISIBLE
                            pbLoading.visibility = View.GONE
                            tvMessage.text = message
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_______exc", "_field_force_check_in_list  : " + e.message)
                    runOnUiThread {
                        rvList.visibility = View.GONE
                        tvMessage.visibility = View.VISIBLE
                        tvRetry.visibility = View.VISIBLE
                        pbLoading.visibility = View.GONE
                        tvMessage.text = "$resp"
                    }
                }
            }
        })
    }

    private fun showList(list: java.util.ArrayList<RvCheckInListModel>) {
        rvList.adapter = RvCheckInListAdapter(list)
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callGetList)
    }


    private fun init() {
        rvList = findViewById(R.id.rvList)
        pbLoading = findViewById(R.id.pbLoading)
        tvRetry = findViewById(R.id.tvRetry)
        tvMessage = findViewById(R.id.tvMessage)
        ivBack = findViewById(R.id.ivBack)
    }
}
