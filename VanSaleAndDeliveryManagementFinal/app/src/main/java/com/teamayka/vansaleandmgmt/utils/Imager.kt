package com.teamayka.vansaleandmgmt.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.teamayka.vansaleandmgmt.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

object Imager {

    fun choosePhoto(activity: Activity, cameraImagePath: String, requestCode: Int) {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val contentUri = FileProvider.getUriForFile(activity, "com.beegains.testdrive.fileprovider", File(cameraImagePath))
        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, contentUri)

        val intents = ArrayList<Intent>()
        addIntentToList(activity, intents, pickIntent)
        addIntentToList(activity, intents, cameraIntent)

        var intent: Intent? = null
        if (intents.size > 0) {
            intent = Intent.createChooser(intents.removeAt(intents.size - 1), "Your pic")
            intent!!.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray<Parcelable>())
        }
        activity.startActivityForResult(intent, requestCode)
    }

    private fun addIntentToList(context: Context, intents: MutableList<Intent>, intent: Intent) {
        val resolveInfoList = context.packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in resolveInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            val targetIntent = Intent(intent)
            targetIntent.`package` = packageName
            intents.add(targetIntent)
        }
    }

    fun getImagePath(activity: Activity, data: Intent?, cameraImagePath: String): String? {
        if (data != null && data.data != null) {
            return getGalleryImageFilePath(activity, data.data) ?: return null
        }

        val imageFile = File(cameraImagePath)
        return if (imageFile.exists() || imageFile.canRead()) {
            imageFile.absolutePath
        } else {
            null
        }
    }

    fun getCameraImageBitmap(data: Intent?): Bitmap? {
        if (data == null)
            return null
        var bitmap: Bitmap? = null
        try {
            bitmap = data.extras!!.get("data") as Bitmap
        } catch (ignored: Exception) {
        }

        return bitmap
    }

    private fun getGalleryImageFilePath(context: Context?, uri: Uri?): String? {
        if (context == null || uri == null)
            return null
        val filePath: String
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        if (cursor == null) {
            filePath = uri.path
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    fun getBitmapFromView(v: View?): Bitmap? {
        if (v == null)
            return null
        //        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true)
        val bitmap = v.getDrawingCache(true).copy(Bitmap.Config.RGB_565, false)
        //        v.setDrawingCacheEnabled(false);
        v.destroyDrawingCache()
        return bitmap
    }

    fun getCompressedFile(imagePath: String, compressedImagePath: String, imageQuality: Int): File? {
        if (TextUtils.isEmpty(imagePath) || TextUtils.isEmpty(compressedImagePath))
            return null

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        // to avoid memory out of bound exception
        BitmapFactory.decodeFile(imagePath, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imageQuality, imageQuality)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false

        var bitmap: Bitmap? = BitmapFactory.decodeFile(imagePath, options) ?: return null

        // check rotation and adjust
        val exif: ExifInterface
        try {
            exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            bitmap = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return fileFromBitmap(bitmap, compressedImagePath)
    }

    private fun fileFromBitmap(bitmap: Bitmap?, compressedImagePath: String): File? {
        if (bitmap == null)
            return null
        val file = File(compressedImagePath)
        return try {
            file.createNewFile()
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            file
        } catch (e: Exception) {
            null
        }
    }

    fun getCompressedBitmapFromFile(imagePath: String, imageQuality: Int): Bitmap? {
        if (TextUtils.isEmpty(imagePath))
            return null

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        // to avoid memory out of bound exception
        BitmapFactory.decodeFile(imagePath, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imageQuality, imageQuality)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false

        var bitmap: Bitmap? = BitmapFactory.decodeFile(imagePath, options) ?: return null

        // check rotation and adjust
        val exif: ExifInterface
        try {
            exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            bitmap = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun loadImage(target: ImageView,
                  url: String,
                  errorImageResource: Int?,
                  clearPrevious: Boolean,
                  showLoading: Boolean) {

        if (target.tag is TagObject) {
            val tagObject = target.tag as TagObject
            val thread = tagObject.thread
            if (thread.isAlive)
                thread.interrupt()
            val progressBar = tagObject.progressBar
            if (progressBar != null)
                (target.parent as ConstraintLayout).removeView(progressBar)
        }

        var progressBar: ProgressBar? = null
        val thread = Thread(Runnable {
            val handler = android.os.Handler(target.context.mainLooper)
            try {
                handler.post {
                    if (clearPrevious)
                        target.setImageDrawable(null)

                    if (showLoading)
                        progressBar = showProgress(target, 60)
                }
                val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                handler.post {
                    target.setImageBitmap(bitmap)
                    removeProgress(target, progressBar)
                }
            } catch (e: Exception) {
                Log.e("_______image load exc", "${e.message}")
                try {
                    handler.post {
                        if (errorImageResource != null)
                            target.setImageResource(errorImageResource)
                        removeProgress(target, progressBar)
                    }
                } catch (e: Exception) {

                }
            }
        })
        target.tag = TagObject(thread, progressBar)
        thread.start()
    }

    private fun removeProgress(target: ImageView, progressBar: ProgressBar?) {
        if (progressBar != null) {
            val p = target.parent as ConstraintLayout
            p.removeView(progressBar)
        }
    }

    private fun showProgress(target: ImageView, size: Int): ProgressBar {
        val layout = target.parent as ConstraintLayout
        val progressBar = ProgressBar(target.context)
        progressBar.id = View.generateViewId()
        layout.addView(progressBar, size, size)
        val set = ConstraintSet()
        set.clone(layout)
        set.connect(progressBar.id, ConstraintSet.START, R.id.ivItem, ConstraintSet.START, 0)
        set.connect(progressBar.id, ConstraintSet.END, R.id.ivItem, ConstraintSet.END, 0)
        set.connect(progressBar.id, ConstraintSet.TOP, R.id.ivItem, ConstraintSet.TOP, 0)
        set.connect(progressBar.id, ConstraintSet.BOTTOM, R.id.ivItem, ConstraintSet.BOTTOM, 0)
        set.applyTo(layout)
        return progressBar
    }

    private class TagObject(
            val thread: Thread,
            val progressBar: ProgressBar?
    )
}