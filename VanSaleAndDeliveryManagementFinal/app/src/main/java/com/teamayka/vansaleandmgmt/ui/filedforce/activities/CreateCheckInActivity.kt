package com.teamayka.vansaleandmgmt.ui.filedforce.activities

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.components.ProgressBar
import com.teamayka.vansaleandmgmt.utils.DataCollections
import com.teamayka.vansaleandmgmt.utils.OkHttpUtils
import com.teamayka.vansaleandmgmt.utils.SnackBarUtils
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.UnknownHostException

class CreateCheckInActivity : AppCompatActivity() {
    private lateinit var tvCustomerName: TextView
    private lateinit var tvCustomerAddress: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvContactPerson: TextView
    private lateinit var tvContactNumber: TextView
    private lateinit var tvLabelTaskDetails: TextView
    private lateinit var tvValueTaskDetails: TextView
    private lateinit var ivImage: ImageView
    private lateinit var bSubmit: Button
    private lateinit var pbLoadingSubmit: ProgressBar
    private lateinit var rgTypes: RadioGroup
    private lateinit var ivBack: ImageView

    private var callSubmitCheckIn: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_check_in)
        init()

        ivBack.setOnClickListener {
            super.onBackPressed()
        }

        (rgTypes.getChildAt(0) as RadioButton).isChecked = true

        ivImage.setOnClickListener {
            showImage()
        }

        bSubmit.setOnClickListener {
            val customerName = tvCustomerName.text.toString().trim()
            val customerAddress = tvCustomerAddress.text.toString().trim()
            val phone = tvPhone.text.toString().trim()
            val contactPerson = tvContactPerson.text.toString().trim()
            val contactNumber = tvContactNumber.text.toString().trim()
            val valueTaskDetails = tvValueTaskDetails.text.toString().trim()

            if (TextUtils.isEmpty(customerName)) {
                SnackBarUtils.showSnackBar(this, "Enter customer name")
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(customerAddress)) {
                SnackBarUtils.showSnackBar(this, "Enter customer address")
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(phone)) {
                SnackBarUtils.showSnackBar(this, "Enter phone")
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(valueTaskDetails)) {
                SnackBarUtils.showSnackBar(this, "Enter task details")
                return@setOnClickListener
            }

            val location = PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_CURRENT_LOCATION", "")
            if (TextUtils.isEmpty(location)) {
                SnackBarUtils.showSnackBar(this, "Can't find location")
                return@setOnClickListener
            } else {
                val latitude = location.split(",")[0]
                val longitude = location.split(",")[1]
                uploadCheckIn(customerName,
                        customerAddress,
                        phone,
                        contactPerson,
                        contactNumber,
                        valueTaskDetails,
                        latitude,
                        longitude)
            }
        }
    }

    private fun uploadCheckIn(customerName: String, customerAddress: String, phone: String, contactPerson: String, contactNumber: String, valueTaskDetails: String, latitude: String, longitude: String) {
        pbLoadingSubmit.visibility = View.VISIBLE
        bSubmit.isEnabled = false

        val client = OkHttpUtils.getOkHttpClient()

        val image = PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_IMAGE_CHECK_IN", "")

        val radioButtonID = rgTypes.checkedRadioButtonId
        val radioButton = rgTypes.findViewById<RadioButton>(radioButtonID)
        val position = rgTypes.indexOfChild(radioButton)

        val r = rgTypes.getChildAt(position) as RadioButton
        val selectedText = r.text.toString()

        val body = FormBody.Builder()
                .add("UserID", DataCollections.getInstance(this).getUser()?.id ?: "")
                .add("CustomerName", customerName)
                .add("CustomerAddress", customerAddress)
                .add("Phone", phone)
                .add("ContactPerson", contactPerson)
                .add("ContactNumber", contactNumber)
                .add("Remarks", valueTaskDetails)
                .add("Image", image)
                .add("Type", when (selectedText) {
                    "Delivery" -> {
                        "1"
                    }
                    "Pickup" -> {
                        "2"
                    }
                    else -> {
                        "3"
                    }
                })
                .add("Latitude", latitude)
                .add("Longitude", longitude)

        val request = Request.Builder()
                .url(PublicUrls.URL_FIELD_FORCE_CREATE_CHECK_IN)
                .post(body.build())
                .build()

        callSubmitCheckIn = client.newCall(request)
        callSubmitCheckIn?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("_____________error", "submit check in : ${e?.message}")
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoadingSubmit.visibility = View.GONE
                    bSubmit.isEnabled = true
                    if (e is UnknownHostException)
                        SnackBarUtils.showSnackBar(this@CreateCheckInActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@CreateCheckInActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp", "submit check in  $resp")
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        val message = jo.getString("Message")

                        runOnUiThread {
                            pbLoadingSubmit.visibility = View.GONE
                            bSubmit.isEnabled = true
                            Toast.makeText(this@CreateCheckInActivity, message, Toast.LENGTH_SHORT).show()
                            PreferenceManager.getDefaultSharedPreferences(this@CreateCheckInActivity).edit().putString("KEY_IMAGE_CHECK_IN", "").apply()
                            finish()
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoadingSubmit.visibility = View.GONE
                            bSubmit.isEnabled = true
                            SnackBarUtils.showSnackBar(this@CreateCheckInActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc", "submit check in" + e.message)
                    runOnUiThread {
                        pbLoadingSubmit.visibility = View.GONE
                        bSubmit.isEnabled = true
                        SnackBarUtils.showSnackBar(this@CreateCheckInActivity, "$resp")
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callSubmitCheckIn)
    }

    private fun showImage() {
        val imageCheckIn = PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_IMAGE_CHECK_IN", "")
        if (TextUtils.isEmpty(imageCheckIn)) {
            val intent = Intent(this, ImageCaptureActivity::class.java)
            intent.putExtra("KEY_IS_CHECK_IN", true)
            startActivity(intent)
        } else {
            val tempImagePath = cacheDir.absolutePath + File.separator + "temp_img_" + packageName + "_12345"
            val imageFile = File("$tempImagePath(1).jpg")
            if (imageFile.exists()) {
                val intent = Intent(this, CapturedImageViewActivity::class.java)
                intent.putExtra("KEY_IMAGE_PATH", imageFile.absolutePath)
                intent.putExtra("KEY_IS_CHECK_IN", true)
                startActivity(intent)
            } else {
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_IMAGE_CHECK_IN", "").apply()
                Toast.makeText(this@CreateCheckInActivity, "Image not exists", Toast.LENGTH_LONG).show()
                val intent = Intent(this, ImageCaptureActivity::class.java)
                intent.putExtra("KEY_IS_CHECK_IN", true)
                startActivity(intent)
            }
        }
    }

    private fun init() {
        tvCustomerName = findViewById(R.id.tvCustomerName)
        tvCustomerAddress = findViewById(R.id.tvCustomerAddress)
        tvPhone = findViewById(R.id.tvPhone)
        tvContactPerson = findViewById(R.id.tvContactPerson)
        tvContactNumber = findViewById(R.id.tvContactNumber)
        tvLabelTaskDetails = findViewById(R.id.tvLabelTaskDetails)
        tvValueTaskDetails = findViewById(R.id.tvValueTaskDetails)
        ivImage = findViewById(R.id.ivImage)
        bSubmit = findViewById(R.id.bSubmit)
        pbLoadingSubmit = findViewById(R.id.pbLoadingSubmit)
        rgTypes = findViewById(R.id.rgTypes)
        ivBack = findViewById(R.id.ivBack)
    }
}
