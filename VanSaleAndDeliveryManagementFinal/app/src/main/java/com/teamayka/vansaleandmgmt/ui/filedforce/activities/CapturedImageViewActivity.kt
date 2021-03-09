package com.teamayka.vansaleandmgmt.ui.filedforce.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.teamayka.vansaleandmgmt.R
import java.io.ByteArrayOutputStream


class CapturedImageViewActivity : AppCompatActivity() {

    private lateinit var btUpload: Button
    private lateinit var btNew: Button
    private lateinit var ivPhoto: SubsamplingScaleImageView
    private lateinit var ivClear: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_captured_image_view)
        init()
        val imagePath = intent.getStringExtra("KEY_IMAGE_PATH")
        ivPhoto.setImage(ImageSource.uri(imagePath))

        val isCheckIn = intent.getBooleanExtra("KEY_IS_CHECK_IN", false)
        btUpload.setOnClickListener {
            val image = getBase64Image(imagePath)
            Log.e("________is check in", " :: :: :: :: :: :: $isCheckIn")
            if (isCheckIn)
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_IMAGE_CHECK_IN", image).apply()
            else
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_IMAGE", image).apply()

            Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        btNew.setOnClickListener {
            if (isCheckIn)
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_IMAGE_CHECK_IN", "").apply()
            else
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_IMAGE", "").apply()
            finish()
            val intent = Intent(this, ImageCaptureActivity::class.java)
            intent.putExtra("KEY_IS_CHECK_IN", isCheckIn)
            startActivity(intent)
        }

        ivClear.setOnClickListener {
            if (isCheckIn)
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_IMAGE_CHECK_IN", "").apply()
            else
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_IMAGE", "").apply()

            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getBase64Image(imagePath: String?): String {
        val bm = BitmapFactory.decodeFile(imagePath)
        val os = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 50, os)
        val imageBytes = os.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun init() {
        ivPhoto = findViewById(R.id.ivPhoto)
        btUpload = findViewById(R.id.btUpload)
        btNew = findViewById(R.id.btNew)
        ivClear = findViewById(R.id.ivClear)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
