package com.teamayka.vansaleandmgmt.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult


/**
 * Created by User on 20-02-2018.
 */
object Locator {
    private const val DELAY_REQUEST_LOCATION_UPDATES_IN_TIME_MILLIS: Long = 1000
    private const val DELAY_REQUEST_LOCATION_UPDATES_IN_METERS: Float = 0.5f

    @SuppressLint("MissingPermission")
    fun trackLocation(context: Context): LocationUpdateListener {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = LocationUpdateListener(context)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DELAY_REQUEST_LOCATION_UPDATES_IN_TIME_MILLIS, DELAY_REQUEST_LOCATION_UPDATES_IN_METERS, listener)
        return listener
    }

    fun cancelLocationTracking(context: Context, listener: LocationListener?) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (listener != null)
            locationManager.removeUpdates(listener)
    }

    class LocationUpdateListener(val context: Context) : LocationListener {
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

        }

        override fun onProviderEnabled(p0: String?) {
        }

        override fun onProviderDisabled(p0: String?) {
        }

        override fun onLocationChanged(location: Location?) {
            if (location == null)
                return
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("KEY_CURRENT_LOCATION", "${location.latitude}, ${location.longitude}").apply()
        }

    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManger = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun requestLocationSettings(activity: Activity) {
        activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    fun requestLocationTurnOnDialog(activity: Activity): PendingResult<LocationSettingsResult> {
        val googleApiClient = GoogleApiClient.Builder(activity).addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        locationRequest.interval = 10000
//        locationRequest.fastestInterval = 10000 / 2
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())

//        result.setResultCallback { r ->
//            when (r.status.statusCode) {
//                LocationSettingsStatusCodes.SUCCESS -> {
//                    Log.e("_________", "location already on")
//                }
//                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
//                    try {
//                        r.status.startResolutionForResult(activity, 1)
//                    } catch (e: Exception) {
//                        Log.e("_________", "can't show location turn on dialog")
//                    }
//                }
//                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
//                    Log.e("_________", "no settings found")
//                }
//            }
//        }

        return result
    }
}