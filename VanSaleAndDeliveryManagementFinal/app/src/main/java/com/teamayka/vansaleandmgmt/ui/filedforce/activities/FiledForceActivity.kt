package com.teamayka.vansaleandmgmt.ui.filedforce.activities

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.firebase.FireBaseNotificationBroadcastReceiver
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import com.teamayka.vansaleandmgmt.utils.*
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.util.*

class FiledForceActivity : AppCompatActivity() {

    private lateinit var tvDate: TextView
    private lateinit var vAllocated: View
    private lateinit var vInProgress: View
    private lateinit var vCompleted: View
    private lateinit var vFailed: View
    private lateinit var vCheckIn: View
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvRetry: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvLabelAllocated: TextView
    private lateinit var tvAllocatedCount: TextView
    private lateinit var tvLabelInProgress: TextView
    private lateinit var tvInProgressCount: TextView
    private lateinit var tvLabelCompleted: TextView
    private lateinit var tvCompletedCount: TextView
    private lateinit var tvLabelFailed: TextView
    private lateinit var tvFailedCount: TextView
    private lateinit var tvCheckIn: TextView
    private lateinit var ivCreateCheckIn: ImageView
    private lateinit var ivBack: ImageView
    private lateinit var ivNotification: ImageView
    private lateinit var tvCheckInCount: TextView

    private lateinit var notificationReceiver: FireBaseNotificationBroadcastReceiver

    private var callGetDashboard: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_field_force)
        init()

        ivBack.setOnClickListener {
            super.onBackPressed()
        }

        tvDate.text = CalendarUtils.getCurrentDate()

        tvDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val pickerDialog = DatePickerDialog.newInstance({ view, year, monthOfYear, dayOfMonth ->
                val date = "%02d/%02d/%04d".format(monthOfYear + 1, dayOfMonth, year)
                tvDate.text = date
                getDashboard(tvDate.text.toString())
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            pickerDialog.accentColor = ResourcesCompat.getColor(resources, R.color.colorBgDefaultBlue, null)
            pickerDialog.show(fragmentManager, "DatePickerDialog")
        }

        notificationReceiver = FireBaseNotificationBroadcastReceiver(ivNotification)
        registerReceiver(notificationReceiver, IntentFilter(Constants.INTENT_FILTER_RECEIVE_NOTIFICATION))
    }

    override fun onResume() {
        super.onResume()

        getDashboard(tvDate.text.toString())
    }

    override fun onPause() {
        super.onPause()
        OkHttpUtils.cancelCalls(callGetDashboard)
    }

    private fun getDashboard(date: String) {
        tvDate.isEnabled = false
        tvMessage.visibility = View.GONE
        tvRetry.visibility = View.GONE
        switchMenuVisibility(View.GONE)
        pbLoading.visibility = View.VISIBLE

        tvRetry.setOnClickListener {
            getDashboard(date)
        }

        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("Date", date)
                .build()

        Log.e("__________post", "_get_dashboard : date : $date")

        val request = Request.Builder()
                .url(PublicUrls.URL_GET_DASHBOARD)
                .post(body)
                .build()

        callGetDashboard = client.newCall(request)
        callGetDashboard?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    tvDate.isEnabled = true
                    switchMenuVisibility(View.GONE)
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
                Log.e("_____________resp", "_get_dashboard : $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val joData = jo.getJSONObject("Data")
                        val allocated = joData.getString("Allocated")
                        val inProgress = joData.getString("InProgress")
                        val completed = joData.getString("Completed")
                        val failed = joData.getString("Failed")
                        val unscheduled = joData.getString("UnScheduled")

                        runOnUiThread {
                            tvDate.isEnabled = true
                            pbLoading.visibility = View.GONE
                            tvRetry.visibility = View.GONE
                            tvMessage.visibility = View.GONE
                            switchMenuVisibility(View.VISIBLE)

                            showMenu(allocated, inProgress, completed, failed, date, unscheduled)
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            tvDate.isEnabled = true
                            switchMenuVisibility(View.GONE)
                            tvMessage.visibility = View.VISIBLE
                            tvRetry.visibility = View.VISIBLE
                            pbLoading.visibility = View.GONE
                            tvMessage.text = message
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_______exc", "_get_dashboard : " + e.message)
                    runOnUiThread {
                        tvDate.isEnabled = true
                        switchMenuVisibility(View.GONE)
                        tvMessage.visibility = View.VISIBLE
                        tvRetry.visibility = View.VISIBLE
                        pbLoading.visibility = View.GONE
                        tvMessage.text = "$resp"
                    }
                }
            }
        })
    }

    private fun showMenu(allocated: String, inProgress: String, completed: String, failed: String, date: String, unscheduled: String) {
        tvAllocatedCount.text = allocated
        tvInProgressCount.text = inProgress
        tvCompletedCount.text = completed
        tvFailedCount.text = failed
        tvCheckInCount.text = unscheduled

        vAllocated.setOnClickListener {
            if (allocated.toInt() <= 0) {
                SnackBarUtils.showSnackBar(this, "No allocated tasks")
                return@setOnClickListener
            }
            val intent = Intent(this, AllocatedActivity::class.java)
            intent.putExtra("KEY_DATE", date)
            startActivity(intent)
        }

        vInProgress.setOnClickListener {
            if (inProgress.toInt() <= 0) {
                SnackBarUtils.showSnackBar(this, "No tasks")
                return@setOnClickListener
            }
            val intent = Intent(this, InProgressActivity::class.java)
            intent.putExtra("KEY_DATE", date)
            startActivity(intent)
        }
        vCompleted.setOnClickListener {
            if (completed.toInt() <= 0) {
                SnackBarUtils.showSnackBar(this, "No completed tasks")
                return@setOnClickListener
            }
            val intent = Intent(this, CompletedActivity::class.java)
            intent.putExtra("KEY_DATE", date)
            startActivity(intent)
        }
        vFailed.setOnClickListener {
            if (failed.toInt() <= 0) {
                SnackBarUtils.showSnackBar(this, "No failed tasks")
                return@setOnClickListener
            }
            val intent = Intent(this, FailedActivity::class.java)
            intent.putExtra("KEY_DATE", date)
            startActivity(intent)
        }

        vCheckIn.setOnClickListener {
            if (unscheduled.toInt() <= 0) {
                SnackBarUtils.showSnackBar(this, "No check in")
                return@setOnClickListener
            }
            val intent = Intent(this, CheckInListActivity::class.java)
            intent.putExtra("KEY_DATE", tvDate.text.toString())
            startActivity(intent)
        }

        ivCreateCheckIn.setOnClickListener {
            val intent = Intent(this, CreateCheckInActivity::class.java)
            startActivity(intent)
        }
    }

    fun switchMenuVisibility(visibility: Int) {
        vAllocated.visibility = visibility
        vInProgress.visibility = visibility
        vCompleted.visibility = visibility
        vFailed.visibility = visibility
        vCheckIn.visibility = visibility
        tvLabelAllocated.visibility = visibility
        tvAllocatedCount.visibility = visibility
        tvLabelInProgress.visibility = visibility
        tvInProgressCount.visibility = visibility
        tvLabelCompleted.visibility = visibility
        tvCompletedCount.visibility = visibility
        tvLabelFailed.visibility = visibility
        tvFailedCount.visibility = visibility
        tvCheckInCount.visibility = visibility
        ivCreateCheckIn.visibility = visibility
        tvCheckIn.visibility = visibility
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callGetDashboard)
        unregisterReceiver(notificationReceiver)
    }


    private fun init() {
        tvDate = findViewById(R.id.tvDate)
        vAllocated = findViewById(R.id.vAllocated)
        vInProgress = findViewById(R.id.vInProgress)
        vCompleted = findViewById(R.id.vCompleted)
        vFailed = findViewById(R.id.vFailed)
        vCheckIn = findViewById(R.id.vCheckIn)
        pbLoading = findViewById(R.id.pbLoading)
        tvRetry = findViewById(R.id.tvRetry)
        tvMessage = findViewById(R.id.tvMessage)
        tvLabelAllocated = findViewById(R.id.tvLabelAllocated)
        tvAllocatedCount = findViewById(R.id.tvAllocatedCount)
        tvLabelInProgress = findViewById(R.id.tvLabelInProgress)
        tvInProgressCount = findViewById(R.id.tvInProgressCount)
        tvLabelCompleted = findViewById(R.id.tvLabelCompleted)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
        tvLabelFailed = findViewById(R.id.tvLabelFailed)
        tvFailedCount = findViewById(R.id.tvFailedCount)
        tvCheckIn = findViewById(R.id.tvCheckIn)
        ivCreateCheckIn = findViewById(R.id.ivCreateCheckIn)
        tvCheckInCount = findViewById(R.id.tvCheckInCount)
        ivBack = findViewById(R.id.ivBack)
        ivNotification = findViewById(R.id.ivNotification)
    }
}
