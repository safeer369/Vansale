package com.teamayka.vansaleandmgmt.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.support.v4.app.ActivityCompat
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

class BarcodeScanningView : SurfaceView {
    var receiverCallBack: BarcodeScanningView.ReceiverCallBack? = null
    var ivFrame: View? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val barcodeDetector = BarcodeDetector.Builder(this.context)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build()

        val cameraSource = CameraSource.Builder(this.context, barcodeDetector)
                .setAutoFocusEnabled(true)
                .build()

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                                    this@BarcodeScanningView.context,
                                    Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    cameraSource.start(holder)
                } catch (e: Exception) {

                }

            }

            override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {

            }

            override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(BarcodeProcessor(barcodeDetector))
    }

    override fun onLayout(b: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        Log.e("__________________", "onLayout()")

        postDelayed({
            var width = 320
            var height = 240

//          Swap width and height sizes when in portrait, since it will be rotated 90 degrees
            if (isPortraitMode) {
                val tmp = width
                width = height
                height = tmp
            }

            val layoutWidth = right - left
            val layoutHeight = bottom - top

            // Computes height and width for potentially doing fit width.
            var childWidth = layoutWidth
            var childHeight = (layoutWidth.toFloat() / width.toFloat() * height).toInt()

//         1.3(HEIGHT) RATIO VIEW
//        if (childHeight > layoutHeight) {
//            childHeight = layoutHeight
//            childWidth = (layoutHeight.toFloat() / height.toFloat() * width).toInt()
//        }

            //FULL SCREEN VIEW
            if (childHeight < layoutHeight) {
                childHeight = layoutHeight
                childWidth = (layoutHeight.toFloat() / height.toFloat() * width).toInt()
            }

            val lp = layoutParams
            lp.width = childWidth
            lp.height = childHeight
            layoutParams = lp
        }, 500)
    }

    private val isPortraitMode: Boolean
        get() {
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true
            }
            return false
        }

    fun setCallBack(receiverCallBack: ReceiverCallBack) {
        this.receiverCallBack = receiverCallBack
    }

    fun setFrame(ivFrame: View?) {
        this.ivFrame = ivFrame
    }

    interface ReceiverCallBack {
        fun onReceiveData(qrValue: String, barcodeDetector: BarcodeDetector, barcodeProcessor: BarcodeProcessor)
    }

    inner class BarcodeProcessor(val barcodeDetector: BarcodeDetector) : Detector.Processor<Barcode> {
        override fun release() {

        }

        override fun receiveDetections(p0: Detector.Detections<Barcode>) {
            val qrCodes = p0.detectedItems
            if (qrCodes.size() > 0) {
                if (ivFrame == null) { // NO FRAME, SO PERFORM FULL SCANNING
                    val qrValue = qrCodes.valueAt(0).displayValue.trim()
                    if (receiverCallBack != null)
                        receiverCallBack!!.onReceiveData(qrValue, barcodeDetector, this)
                } else { // HERE FRAME, SO PERFORM CROPPED SCANNING
                    val frameWidth = p0.frameMetadata.width
                    val frameHeight = p0.frameMetadata.height

                    val parentWidth = (ivFrame!!.parent as View).width
                    val parentHeight = (ivFrame!!.parent as View).height

                    val exactWidthRatio = (parentWidth.toFloat() / frameWidth.toFloat())
                    val exactHeightRatio = (parentHeight.toFloat() / frameHeight.toFloat())

                    val boundaryLeft = ivFrame!!.left
                    val boundaryRight = ivFrame!!.right
                    val boundaryTop = ivFrame!!.top
                    val boundaryBottom = ivFrame!!.bottom

                    for (i in 0 until qrCodes.size()) {
                        val barcode = qrCodes.valueAt(i)
                        val barcodeLeft = barcode.boundingBox.left * exactWidthRatio
                        val barcodeRight = barcode.boundingBox.right * exactWidthRatio
                        val barcodeTop = barcode.boundingBox.top * exactHeightRatio
                        val barcodeBottom = barcode.boundingBox.bottom * exactHeightRatio
                        if (barcodeLeft > boundaryLeft && barcodeTop > boundaryTop && barcodeRight < boundaryRight && barcodeBottom < boundaryBottom) {
                            val qrValue = barcode.displayValue.trim()
                            if (receiverCallBack != null)
                                receiverCallBack!!.onReceiveData(qrValue, barcodeDetector, this)
                            break
                        }
                    }
                }
            }
        }

    }
}