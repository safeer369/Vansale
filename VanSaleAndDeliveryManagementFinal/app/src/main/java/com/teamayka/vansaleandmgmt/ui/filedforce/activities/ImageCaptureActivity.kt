package com.teamayka.vansaleandmgmt.ui.filedforce.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.utils.PermissionUtils
import java.io.File

class ImageCaptureActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_CAPTURE_IMAGE = 5
        private const val REQUEST_CODE_PERMISSION_CAMERA = 32
    }

    private lateinit var tempImagePath: String

    var isCheckIn = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deliverey_note_image_capture)
        tempImagePath = cacheDir.absolutePath + File.separator + "temp_img_" + packageName + "_12345"

        isCheckIn = intent.getBooleanExtra("KEY_IS_CHECK_IN", false)

        val permissions = arrayOf(android.Manifest.permission.CAMERA)
        if (PermissionUtils.hasAllPermissions(this, permissions)) {
            takeImage(isCheckIn)
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION_CAMERA)
        }
    }

    private fun takeImage(checkIn: Boolean) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val file = File("$tempImagePath(1).jpg")
        val uri = FileProvider.getUriForFile(this@ImageCaptureActivity, "com.teamayka.vansaleandmgmt.fileprovider", file)
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_CAPTURE_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val imageFile = File("$tempImagePath(1).jpg")
                    if (imageFile.exists()) {
                        val intent = Intent(this, CapturedImageViewActivity::class.java)
                        intent.putExtra("KEY_IMAGE_PATH", imageFile.absolutePath)
                        intent.putExtra("KEY_IS_CHECK_IN", isCheckIn)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@ImageCaptureActivity, "Can't capture image", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } else {
                    finish()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_CAMERA -> {
                if (PermissionUtils.hasAllPermissionsGranted(grantResults)) {
                    takeImage(isCheckIn)
                } else {
                    Toast.makeText(this@ImageCaptureActivity, "Can't capture image Give Permission", Toast.LENGTH_LONG).show()
                    finish()

                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
