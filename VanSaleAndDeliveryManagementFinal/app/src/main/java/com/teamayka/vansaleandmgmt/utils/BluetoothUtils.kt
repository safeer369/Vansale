package com.example.basil.zebraprinttest

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * Created by seydbasil on 4/24/2018.
 */
class BluetoothUtils(val listener: BluetoothListener) {
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var isTurningBluetooth = false

    private fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    fun turnOn(context: Context) {
        if (isBluetoothEnabled()) {
            listener.onSuccess(true)
            return
        }
        bluetoothAdapter.enable()
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {

        }
        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        isTurningBluetooth = true
    }

    fun turnOff(context: Context) {
        if (!isBluetoothEnabled()) {
            listener.onSuccess(false)
            return
        }
        bluetoothAdapter.disable()
        try { // should use try for registered exception
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {

        }
        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        isTurningBluetooth = true
    }

    interface BluetoothListener {
        fun onSuccess(isOn: Boolean)
        fun onError()
    }

    fun isTurning(): Boolean {
        return isTurningBluetooth
    }

    fun cancelListener(context: Context) {
        try { // should use try for registered exception
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {

        }
        isTurningBluetooth = false
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                try {
                    context?.unregisterReceiver(this)
                } catch (e: Exception) {

                }
                return
            }
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        try {
                            context?.unregisterReceiver(this)
                        } catch (e: Exception) {

                        }
                        isTurningBluetooth = false
                        listener.onSuccess(false)
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> {
                    }
                    BluetoothAdapter.STATE_ON -> {
                        try {
                            context?.unregisterReceiver(this)
                        } catch (e: Exception) {

                        }
                        isTurningBluetooth = false
                        listener.onSuccess(true)
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> {
                    }
                    else -> {
                        try {
                            context?.unregisterReceiver(this)
                        } catch (e: Exception) {

                        }
                        isTurningBluetooth = false
                        listener.onError()
                    }
                }
            }
        }
    }
}