package com.teamayka.vansaleandmgmt.utils

import android.app.Activity
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Tools {

    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
    }

    fun saveImageToPDF(title: View, bitmap: Bitmap, filename: String) {

        val mFolder = File("")
        val mFile = File(mFolder, "$filename.pdf")
        if (!mFile.exists()) {
            val height = title.height + bitmap.height
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, height, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            title.draw(canvas)

            canvas.drawBitmap(bitmap, null, Rect(0, title.height, bitmap.width, bitmap.height), null)

            document.finishPage(page)

            try {
                mFile.createNewFile()
                val out = FileOutputStream(mFile)
                document.writeTo(out)
                document.close()
                out.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }

    fun getRecyclerViewScreenshot(view: RecyclerView): Bitmap {
        val size = view.adapter.itemCount
        val holder = view.adapter.createViewHolder(view, 0)
        view.adapter.onBindViewHolder(holder, 0)
        holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        holder.itemView.layout(0, 0, holder.itemView.measuredWidth, holder.itemView.measuredHeight)
        val bigBitmap = Bitmap.createBitmap(view.measuredWidth, holder.itemView.measuredHeight * size,
                Bitmap.Config.ARGB_8888)
        val bigCanvas = Canvas(bigBitmap)
        bigCanvas.drawColor(Color.WHITE)
        val paint = Paint()
        var iHeight = 0
        holder.itemView.isDrawingCacheEnabled = true
        holder.itemView.buildDrawingCache()
        bigCanvas.drawBitmap(holder.itemView.drawingCache, 0f, iHeight.toFloat(), paint)
        holder.itemView.isDrawingCacheEnabled = false
        holder.itemView.destroyDrawingCache()
        iHeight += holder.itemView.measuredHeight
        for (i in 1 until size) {
            view.adapter.onBindViewHolder(holder, i)
            holder.itemView.isDrawingCacheEnabled = true
            holder.itemView.buildDrawingCache()
            bigCanvas.drawBitmap(holder.itemView.drawingCache, 0f, iHeight.toFloat(), paint)
            iHeight += holder.itemView.measuredHeight
            holder.itemView.isDrawingCacheEnabled = false
            holder.itemView.destroyDrawingCache()
        }
        return bigBitmap
    }
}
