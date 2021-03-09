package com.teamayka.vansaleandmgmt.ui.filedforce.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.widget.Button
import android.widget.Toast
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.components.SignatureView
import java.io.ByteArrayOutputStream

class SignatureActivity : AppCompatActivity() {

    lateinit var signature_pad: SignatureView
    lateinit var btClear: Button
    lateinit var btSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_signature)
        init()

        btClear.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_SIGNATURE", "").apply()
            signature_pad.clearSignature()
        }

        btSave.setOnClickListener {
            val signature = getSignature()
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_SIGNATURE", signature).apply()
            Toast.makeText(this, "Signature saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getSignature(): String {
        val bm = signature_pad.image
        val os = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 50, os)
        val imageBytes = os.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun init() {
        signature_pad = findViewById(R.id.signature_pad)
        btClear = findViewById(R.id.btClear)
        btSave = findViewById(R.id.btSave)
    }
}
