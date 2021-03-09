package com.teamayka.vansaleandmgmt.ui.filedforce.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.filedforce.models.RvListModel


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var ivBack: ImageView

    private var ticketDate = ""
    private var isCompleted = false
    private var isFailed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        init()

        ivBack.setOnClickListener {
            super.onBackPressed()
        }

        ticketDate = intent.getStringExtra("KEY_DATE")
        isCompleted = intent.getBooleanExtra("KEY_IS_COMPLETED", false)
        isFailed = intent.getBooleanExtra("KEY_IS_FAILED", false)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun init() {
        ivBack = findViewById(R.id.ivBack)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val currentLocation = PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_CURRENT_LOCATION", "")
        if (!TextUtils.isEmpty(currentLocation)) {
            val marker = MarkerOptions()
            marker.position(LatLng(currentLocation.split(",")[0].toDouble(), currentLocation.split(",")[1].toDouble()))
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_location))
            mMap.addMarker(marker)
        }

        val list = intent.getParcelableArrayListExtra<RvListModel>("KEY_LIST")
        val bounds = LatLngBounds.Builder()
        for (i in 0 until list.size) {
            val location = LatLng(list[i].latitude.toDouble(), list[i].longitude.toDouble())
            bounds.include(location)
            val marker = mMap.addMarker(createMarker(location, i, list[i].ticketIDNo))
            marker.tag = i
        }

        mMap.setOnMapLoadedCallback {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 10))

            mMap.setOnMarkerClickListener {
                if ((it.tag is Int)) {
                    val position = it.tag as Int
                    val intent = Intent(this, ViewTicketActivity::class.java)
                    intent.putExtra("KEY_CUSTOMER_NAME", list[position].customer)
                    intent.putExtra("KEY_PHONE_1", list[position].phone1)
                    intent.putExtra("KEY_PHONE_2", list[position].phone2)
                    intent.putExtra("KEY_ADDRESS", list[position].address)
                    intent.putExtra("KEY_TICKET_DESCRIPTION", list[position].ticketDescription)
                    intent.putExtra("KEY_ORDER_INFO", list[position].orderInfo)
                    intent.putExtra("KEY_TICKET_ID", list[position].ticketId)
                    intent.putExtra("KEY_TICKET_ID_NO", list[position].ticketIDNo)
                    intent.putExtra("KEY_LATITUDE", list[position].latitude)
                    intent.putExtra("KEY_LONGITUDE", list[position].longitude)
                    intent.putExtra("KEY_STATUS_ID", list[position].statusId)
                    intent.putExtra("KEY_REMARKS", list[position].remarks)
                    intent.putExtra("KEY_RECEIVED_BY", list[position].receivedBy)
                    intent.putExtra("KEY_DATE", ticketDate)
                    intent.putExtra("KEY_IS_COMPLETED", isCompleted)
                    intent.putExtra("KEY_IS_FAILED", isFailed)
                    startActivity(intent)
                }
                true
            }
        }
    }

    private fun createMarker(location: LatLng, position: Int, ticketIDNo: String): MarkerOptions {
        val marker = MarkerOptions()
        marker.position(location)
        val tv = TextView(this)
        tv.text = ("%s\n%s").format((position + 1).toString(), ticketIDNo)
        tv.gravity = Gravity.CENTER
        tv.setPadding(resources.getDimension(R.dimen.paddingStart).toInt(), resources.getDimension(R.dimen.paddingTop).toInt(), resources.getDimension(R.dimen.paddingEnd).toInt(), resources.getDimension(R.dimen.paddingBottom).toInt())
        tv.setBackgroundResource(R.drawable.ic_marker)
        tv.setTextColor(Color.parseColor("#FFFFFFFF"))
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        tv.layout(0, 0, tv.measuredWidth, tv.measuredHeight)
        tv.isDrawingCacheEnabled = true
        tv.buildDrawingCache()
        val mDotMarkerBitmap = Bitmap.createBitmap(tv.measuredWidth, tv.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mDotMarkerBitmap)
        tv.draw(canvas)
        marker.icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
        tv.destroyDrawingCache()
        tv.isDrawingCacheEnabled = false
        return marker
    }
}
