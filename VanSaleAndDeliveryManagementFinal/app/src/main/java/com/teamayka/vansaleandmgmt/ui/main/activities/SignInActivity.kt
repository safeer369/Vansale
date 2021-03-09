package com.teamayka.vansaleandmgmt.ui.main.activities

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.PublicUrls
import com.teamayka.vansaleandmgmt.utils.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.util.*
import kotlin.collections.ArrayList

class SignInActivity : AppCompatActivity() {

    private lateinit var layoutParent: View
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvSignIn: TextView
    private lateinit var pbLoading: ImageView

    private var callLogin: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        init()

        // CLEAR DATA BASED ON DAY
        val storedDate = PreferenceManager.getDefaultSharedPreferences(this).getString("KEY_DATE", "")
        if (storedDate != getCurrentDate()) {
            DataCollections.getInstance(this).clearSalesInvoiceTable()
            DataCollections.getInstance(this).clearSalesOrderInvoiceTable()
            DataCollections.getInstance(this).clearPurchaseInvoiceTable()
            DataCollections.getInstance(this).clearSalesReturnInvoiceTable()
            DataCollections.getInstance(this).clearPurchaseReturnInvoiceTable()
            DataCollections.getInstance(this).clearInProgressListTable()
            DataCollections.getInstance(this).clearFailedListTable()
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("KEY_DATE", getCurrentDate()).apply()

        val user = DataCollections.getInstance(this).getUser()
        if (user != null) {
            if (user.role == LoginVariables.LOGIN_VAR_THREE) {
                startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@SignInActivity, GetCustomersActivity::class.java))
                finish()
            }
        }

        tvSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                SnackBarUtils.showSnackBar(this, "Please enter username and password")
            } else
                doSignIn(email, password)
        }
    }

    private fun doSignIn(userId: String, password: String) {
        pbLoading.visibility = View.VISIBLE
        tvSignIn.isEnabled = false

        val client = OkHttpUtils.getOkHttpClient()

        val jsonObject = JSONObject()
        jsonObject.put("username", userId)
        jsonObject.put("password", password)

        Log.e("_____________login_req", jsonObject.toString())

        val body = FormBody.Builder()
                .add("Login", jsonObject.toString())
                .build()

        val url = "https://api.myjson.com/bins/195clo"
        val request = Request.Builder()
                .url(PublicUrls.URL_USER_LOGIN)
                .post(body)
                .build()

        callLogin = client.newCall(request)
        callLogin?.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                if (call == null || call.isCanceled)
                    return
                runOnUiThread {
                    pbLoading.visibility = View.GONE
                    tvSignIn.isEnabled = true
                    if (e is UnknownHostException)
                        SnackBarUtils.showSnackBar(this@SignInActivity, getString(R.string.error_message_connect_error))
                    else
                        SnackBarUtils.showSnackBar(this@SignInActivity, getString(R.string.error_message_went_wront))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                if (call == null || call.isCanceled)
                    return
                val resp = response?.body()?.string()
                Log.e("_____________resp_login", resp)
                try {
                    val jo = JSONObject(resp)
                    val result = jo.getBoolean("Result")
                    if (result) {
                        runOnUiThread {
                            pbLoading.visibility = View.GONE
                            tvSignIn.isEnabled = true

                            val data = jo.getJSONObject("Data")
                            val userKey = data.getString("UserID")
                            val role = data.getString("UserRole")
                            val name = data.getString("UserName")
                            val imageUrl = data.getString("ImageUrl")
                            val headerList = ArrayList<String>()
                            val footerList = ArrayList<String>()
                            val jaHeader = jo.getJSONArray("Header")
                            val jaFooter = jo.getJSONArray("Footer")
                            for (i in 0 until jaHeader.length()) {
                                headerList.add(jaHeader.getString(i))
                            }

                            for (i in 0 until jaFooter.length()) {
                                footerList.add(jaHeader.getString(i))
                            }

//                            footerList.add("Test string")
//                            footerList.add("Test string1")
//                            footerList.add("Test string2")

                            val header = StringUtils.serialize(headerList)
                            val footer = StringUtils.serialize(footerList)

                            val amountType = data.getInt("CalculationType")

                            DataCollections.getInstance(this@SignInActivity).setUser(userKey, role, name, imageUrl, amountType, header, footer)

                            if (role == LoginVariables.LOGIN_VAR_THREE) {
                                startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                                finish()
                            } else {
                                startActivity(Intent(this@SignInActivity, GetCustomersActivity::class.java))
                                finish()
                            }
                        }
                    } else {
                        val message = jo.getString("Message")
                        runOnUiThread {
                            pbLoading.visibility = View.GONE
                            tvSignIn.isEnabled = true
                            SnackBarUtils.showSnackBar(this@SignInActivity, message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("_____________exc_login", e.message)
                    runOnUiThread {
                        pbLoading.visibility = View.GONE
                        tvSignIn.isEnabled = true
                        SnackBarUtils.showSnackBar(this@SignInActivity, "$resp")
                    }
                }
            }
        })
    }

    private fun getCurrentDate(): String {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        return day.toString() + " " + year
    }

    override fun onDestroy() {
        super.onDestroy()
        OkHttpUtils.cancelCalls(callLogin)
    }

    private fun init() {
        layoutParent = findViewById(R.id.layoutParent)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvSignIn = findViewById(R.id.tvSignIn)
        pbLoading = findViewById(R.id.pbLoading)
    }
}
