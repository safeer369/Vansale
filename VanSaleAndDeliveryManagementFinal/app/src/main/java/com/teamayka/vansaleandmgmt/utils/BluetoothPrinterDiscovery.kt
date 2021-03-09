package com.teamayka.vansaleandmgmt.utils

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.printer.discovery.DiscoveryHandler

class BluetoothPrinterDiscovery(private val listener: PrinterDiscoverListener) {
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var discoveredPrinter: DiscoveredPrinter? = null
    private var isCancelledByUser = false

    fun discover(context: Context) {
        if (discoveredPrinter != null) {
            listener.onSuccess(discoveredPrinter!!)
            return
        }

        val discoveryHandler = object : DiscoveryHandler {
            override fun discoveryFinished() {
                if (isCancelledByUser) {
                    isCancelledByUser = false
                    return
                }
                if (discoveredPrinter != null) {
                    listener.onSuccess(discoveredPrinter!!)
                } else {
                    listener.onFail()
                }
            }

            override fun foundPrinter(discoveredPrinter: DiscoveredPrinter?) {
                // below code will finish discovery if a printer found.
                if (discoveredPrinter != null) {
                    this@BluetoothPrinterDiscovery.discoveredPrinter = discoveredPrinter
                    listener.onSuccess(discoveredPrinter)
                    cancelDiscovery()
                }
            }

            override fun discoveryError(p0: String?) {
                if (isCancelledByUser) {
                    isCancelledByUser = false
                    return
                }
                listener.onFail()
            }
        }
        BluetoothDiscoverer.findPrinters(context, discoveryHandler)
    }

    fun isDiscovering(): Boolean {
        return bluetoothAdapter.isDiscovering
    }

    fun cancelDiscovery() {
        isCancelledByUser = true
        if (isDiscovering())
            bluetoothAdapter.cancelDiscovery()
    }

    interface PrinterDiscoverListener {
        fun onSuccess(discoveredPrinter: DiscoveredPrinter)
        fun onFail()
    }
}