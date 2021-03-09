package com.teamayka.vansaleandmgmt.ui.main.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.utils.OkHttpUtils
import com.teamayka.vansaleandmgmt.utils.PermissionUtils
import com.teamayka.vansaleandmgmt.utils.SnackBarUtils
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class RegistrationActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSION_CAMERA = 10
    }

    private lateinit var etActivationKey: TextView
    private lateinit var etBuildKey: TextView
    private lateinit var etName: TextView
    private lateinit var etPhone: TextView
    private lateinit var bRegister: Button
    private lateinit var pbLoading: ProgressBar

    private var callRegister: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        init()

        val isRegistered = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("KEY_IS_REGISTERED", false)
        if (isRegistered) {
            startActivity(Intent(this@RegistrationActivity, SignInActivity::class.java))
            finish()
        }

        val permission = arrayOf(android.Manifest.permission.READ_PHONE_STATE)
        if (PermissionUtils.hasAllPermissions(this, permission)) {
            getDeviceId()
        } else {
            // FOR FRAGMENT USE requestPermissions function NOT ActivityCompat.requestPermissions
            ActivityCompat.requestPermissions(this, permission, REQUEST_CODE_PERMISSION_CAMERA)
        }

        bRegister.setOnClickListener {
            val activationKey = etActivationKey.text.toString().trim()
            val buildKey = etBuildKey.text.toString().trim()
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            if (TextUtils.isEmpty(activationKey) ||
                    TextUtils.isEmpty(buildKey) ||
                    TextUtils.isEmpty(name) ||
                    TextUtils.isEmpty(phone)) {
                SnackBarUtils.showSnackBar(this, "Please fill")
                return@setOnClickListener
            }

            doRegister(activationKey,
                    buildKey,
                    name,
                    phone)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceId() {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val iMEI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tm.imei
        } else {
            tm.deviceId
        }

        if (iMEI == null) {
            etBuildKey.text = ("%s").format("Could not read device id")
            return
        } else
            etBuildKey.text = iMEI

    }

    private fun doRegister(activationKey: String, buildKey: String, name: String, phone: String) {
        pbLoading.visibility = View.VISIBLE
        bRegister.isEnabled = false

        val client = OkHttpUtils.getOkHttpClient()

        val jsonObject = JSONObject()
        jsonObject.put("ActivationKey", activationKey)
        jsonObject.put("BuildKey", buildKey)
        jsonObject.put("FullName", name)
        jsonObject.put("Mobile", phone)

        Log.e("__________register_req", jsonObject.toString())

        val body = FormBody.Builder()
                .add("Register", jsonObject.toString())
                .build()

        val request = Request.Builder()
                .url(PublicUrls.URL_USER_REGISTER)
                .post(body)
                .build()

        callRegister = client.newCall(request)
        callRegister?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoading.visibility = View.GONE
                    bRegister.isEnabled = true
                    if (e is UnknownHostException)
                        SnackBarUtils.showSnackBar(this@RegistrationActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@RegistrationActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_________resp_register", resp)
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        runOnUiThread {
                            pbLoading.visibility = View.GONE
                            bRegister.isEnabled = true

                            PreferenceManager.getDefaultSharedPreferences(this@RegistrationActivity).edit().putBoolean("KEY_IS_REGISTERED", true).apply()

                            val intent = Intent(this@RegistrationActivity, SignInActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoading.visibility = View.GONE
                            bRegister.isEnabled = true
                            SnackBarUtils.showSnackBar(this@RegistrationActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("________exc_register", e.message)
                    runOnUiThread {
                        pbLoading.visibility = View.GONE
                        bRegister.isEnabled = true
                        SnackBarUtils.showSnackBar(this@RegistrationActivity, "$resp")
                    }
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_CAMERA -> {
                if (PermissionUtils.hasAllPermissionsGranted(grantResults)) {
                    getDeviceId()
                } else {
                    SnackBarUtils.showSnackBar(this, "Can't get device id")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callRegister)
    }

    private fun init() {
        etActivationKey = findViewById(R.id.etActivationKey)
        etBuildKey = findViewById(R.id.etBuildKey)
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        bRegister = findViewById(R.id.bRegister)
        pbLoading = findViewById(R.id.pbLoading)
    }
}
