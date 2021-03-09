package com.teamayka.vansaleandmgmt.ui.main.activities

import android.app.Activity
import android.content.Intent
import android.location.LocationListener
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.firebase.iid.FirebaseInstanceId
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.filedforce.activities.FiledForceActivity
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import com.teamayka.vansaleandmgmt.ui.main.adapters.MainRvAdapter
import com.teamayka.vansaleandmgmt.ui.purchase.activities.PurchaseActivity
import com.teamayka.vansaleandmgmt.ui.purchasereturn.activities.PurchaseReturnActivity
import com.teamayka.vansaleandmgmt.ui.sales.activities.SalesActivity
import com.teamayka.vansaleandmgmt.ui.salesorder.activities.SalesOrderActivity
import com.teamayka.vansaleandmgmt.ui.salesreport.SalesReportActivity
import com.teamayka.vansaleandmgmt.ui.salesreturn.activities.SalesReturnActivity
import com.teamayka.vansaleandmgmt.ui.stockreport.StockCheckActivity
import com.teamayka.vansaleandmgmt.utils.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSION_LOCATION = 14
        private const val REQUEST_CODE_ACTIVITY_RESULT_LOCATION_TURN_ON = 12
    }

    private lateinit var dlMain: DrawerLayout
    private lateinit var ivClose: ImageView
    private lateinit var tvLogout: TextView
    private lateinit var ivMenu: ImageView
    private lateinit var rvMain: RecyclerView
    private lateinit var rvLeftMenu: RecyclerView
    private lateinit var tvUsername: TextView
    private lateinit var ivProfile: ImageView

    var locationListener: LocationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        ivMenu.setOnClickListener {
            dlMain.openDrawer(Gravity.START)
        }

        ivClose.setOnClickListener {
            dlMain.closeDrawer(Gravity.START)
        }

        tvLogout.setOnClickListener {
            DataCollections.getInstance(this).clearUserTable()
            DataCollections.getInstance(this).clearProducts()
            DataCollections.getInstance(this).clearCustomers()
            DataCollections.getInstance(this).clearSuppliers()
            DataCollections.getInstance(this).clearSalesInvoiceTable()
            DataCollections.getInstance(this).clearSalesOrderInvoiceTable()
            DataCollections.getInstance(this).clearPurchaseInvoiceTable()
            DataCollections.getInstance(this).clearSalesReturnInvoiceTable()
            DataCollections.getInstance(this).clearPurchaseReturnInvoiceTable()
            DataCollections.getInstance(this).clearInProgressListTable()
            DataCollections.getInstance(this).clearFailedListTable()
            DataCollections.getInstance(this).clearFcmSyncStatusTable()
            finish()
            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
        }

        val list = ArrayList<String>()
        val user = DataCollections.getInstance(this).getUser()
        tvUsername.text = user?.name
        when (user!!.role) {
            LoginVariables.LOGIN_VAR_ONE -> {
                list.add("Sales")
                list.add("Sales return")
                list.add("Purchase")
                list.add("Purchase return")
                list.add("Sales order")
                list.add("Stock report")
                list.add("Sales report")
            }
            LoginVariables.LOGIN_VAR_TWO -> {
                list.add("Sales")
                list.add("Sales return")
                list.add("Purchase")
                list.add("Purchase return")
                list.add("Sales order")
                list.add("Stock report")
                list.add("Sales report")
                list.add("Field force")
            }
            LoginVariables.LOGIN_VAR_THREE -> {
                list.add("Field force")
            }
        }

        setBackground(list)
        rvMain.layoutManager = GridLayoutManager(this, 2)
        rvMain.adapter = MainRvAdapter(list, this)

        rvLeftMenu.adapter = LeftMenuRvAdapter(list)

        if (user.role == LoginVariables.LOGIN_VAR_THREE)
            performItemClick(list[0]) // Field force

        val exactFcm = FirebaseInstanceId.getInstance().token
        val fcm = DataCollections.getInstance(this).getFcm()
        if (fcm == exactFcm) {
            if (!DataCollections.getInstance(this).isFcmUpdated() && fcm != null) {
                updateFCM(user.id, fcm)
            }
        } else {
            if (exactFcm != null) {
                DataCollections.getInstance(this).updateFcmSyncStatus(exactFcm, false)
                updateFCM(user.id, exactFcm)
                return
            }

            if (fcm != null && !DataCollections.getInstance(this).isFcmUpdated())
                updateFCM(user.id, fcm)
        }
    }

    private fun setBackground(list: ArrayList<String>) {
        if (list.size % 4 == 3)
            rvMain.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorBgBlueLight, theme))
        else if (list.size % 4 == 1) {
            rvMain.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorBgBlueVeryLight, theme))
        }
    }

    inner class LeftMenuRvAdapter(private val list: ArrayList<String>) : RecyclerView.Adapter<LeftMenuRvAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_left_main_menu, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvText.text = list[position]
            setItemIcon(list[position], holder.ivIcon)

            holder.layoutParent.setOnClickListener {
                dlMain.closeDrawer(Gravity.START)
                dlMain.addDrawerListener(object : DrawerLayout.DrawerListener {
                    override fun onDrawerStateChanged(newState: Int) {

                    }

                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    }

                    override fun onDrawerClosed(drawerView: View) {
                        dlMain.removeDrawerListener(this)
                        performItemClick(list[holder.adapterPosition])
                    }

                    override fun onDrawerOpened(drawerView: View) {

                    }
                })
            }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val layoutParent: View = itemView.findViewById(R.id.layoutParent)
            val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
            val tvText: TextView = itemView.findViewById(R.id.tvText)
        }
    }

    fun setItemIcon(s: String, ivIcon: ImageView) {
        when (s) {
            "Sales" -> {
                ivIcon.setImageResource(R.drawable.ic_sales)
            }
            "Sales return" -> {
                ivIcon.setImageResource(R.drawable.ic_sales_return)
            }
            "Purchase" -> {
                ivIcon.setImageResource(R.drawable.ic_purchase)
            }
            "Purchase return" -> {
                ivIcon.setImageResource(R.drawable.ic_purchase_return)
            }
            "Sales order" -> {
                ivIcon.setImageResource(R.drawable.ic_sales_order)
            }
            "Stock report" -> {
                ivIcon.setImageResource(R.drawable.ic_stock_report)
            }
            "Sales report" -> {
                ivIcon.setImageResource(R.drawable.ic_sales_report)
            }
            "Field force" -> {
                ivIcon.setImageResource(R.drawable.ic_delivery)
            }
        }
    }

    fun performItemClick(s: String) {
        when (s) {
            "Sales" -> {
                val intent = Intent(this, SalesActivity::class.java)
                startActivity(intent)
            }
            "Sales return" -> {
                val intent = Intent(this, SalesReturnActivity::class.java)
                startActivity(intent)
            }
            "Purchase" -> {
                val intent = Intent(this, PurchaseActivity::class.java)
                startActivity(intent)
            }
            "Purchase return" -> {
                val intent = Intent(this, PurchaseReturnActivity::class.java)
                startActivity(intent)
            }
            "Sales order" -> {
                val intent = Intent(this, SalesOrderActivity::class.java)
                startActivity(intent)
            }
            "Stock report" -> {
                val intent = Intent(this, StockCheckActivity::class.java)
                startActivity(intent)
            }
            "Sales report" -> {
                val intent = Intent(this, SalesReportActivity::class.java)
                startActivity(intent)
            }
            "Field force" -> {
                val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                if (PermissionUtils.hasAllPermissions(this, permissions)) {
                    val result = Locator.requestLocationTurnOnDialog(this)
                    result.setResultCallback { r ->
                        when (r.status.statusCode) {
                            LocationSettingsStatusCodes.SUCCESS -> {
                                Log.e("_________", "location already on")
                                locationListener = Locator.trackLocation(this)
                                val intent = Intent(this, FiledForceActivity::class.java)
                                startActivity(intent)
                            }
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                                try {
                                    r.status.startResolutionForResult(this, REQUEST_CODE_ACTIVITY_RESULT_LOCATION_TURN_ON)
                                } catch (e: Exception) {
                                    Log.e("_________", "can't show location turn on dialog")
                                    Toast.makeText(this, "No location settings found. Please turn on location", Toast.LENGTH_LONG).show()
                                }
                            }
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                Log.e("_________", "no settings found")
                                Toast.makeText(this, "No location settings found. Please turn on location", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION_LOCATION)
                }
            }
        }
    }

    //USING THIS FOR FIELD FORCE ONLY
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_LOCATION -> {
                if (PermissionUtils.hasAllPermissionsGranted(grantResults)) {
                    val result = Locator.requestLocationTurnOnDialog(this)
                    result.setResultCallback { r ->
                        when (r.status.statusCode) {
                            LocationSettingsStatusCodes.SUCCESS -> {
                                Log.e("_________", "location already on")
                                locationListener = Locator.trackLocation(this)
                                val intent = Intent(this, FiledForceActivity::class.java)
                                startActivity(intent)
                            }
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                                try {
                                    r.status.startResolutionForResult(this, REQUEST_CODE_ACTIVITY_RESULT_LOCATION_TURN_ON)
                                } catch (e: Exception) {
                                    Log.e("_________", "can't show location turn on dialog")
                                    Toast.makeText(this, "No location settings found. Please turn on location", Toast.LENGTH_LONG).show()
                                }
                            }
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                Log.e("_________", "no settings found")
                                Toast.makeText(this, "No location settings found. Please turn on location", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "No location permission", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    //USING THIS FOR FIELD FORCE ONLY
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("________________", "requestCode : $requestCode,  resultCode : $resultCode, data : $data")
        when (requestCode) {
            REQUEST_CODE_ACTIVITY_RESULT_LOCATION_TURN_ON -> {
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                } else {
                    locationListener = Locator.trackLocation(this)
                    val intent = Intent(this, FiledForceActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun updateFCM(user: String, fcm: String) {
        val client = OkHttpUtils.getOkHttpClient()

        val body = FormBody.Builder()
                .add("UserID", user)
                .add("FCMID", fcm)
                .build()

        val request = Request.Builder()
                .url(PublicUrls.URL_UPDATE_FCM_ID)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                Log.e("___________fail", "_update_fcm: ${e?.message}")
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_______resp_update_fcm", resp)
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        DataCollections.getInstance(this@MainActivity).updateFcmSyncStatus(fcm, true)
                    }
                } catch (e: Exception) {
                    Log.e("________ex", "update fcm :" + e.message)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Locator.cancelLocationTracking(this, locationListener)
    }

    private fun init() {
        dlMain = findViewById(R.id.dlMain)
        ivClose = findViewById(R.id.ivClose)
        tvLogout = findViewById(R.id.tvLogout)
        ivMenu = findViewById(R.id.ivMenu)
        rvMain = findViewById(R.id.rvMain)
        rvLeftMenu = findViewById(R.id.rvLeftMenu)
        tvUsername = findViewById(R.id.tvUsername)
        ivProfile = findViewById(R.id.ivProfile)
    }
}
