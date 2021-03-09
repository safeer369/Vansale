package com.teamayka.vansaleandmgmt.ui.filedforce.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.teamayka.vansaleandmgmt.R

class ViewCheckInActivity : AppCompatActivity() {
    private lateinit var tvCustomerName: TextView
    private lateinit var tvCustomerAddress: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvContactPerson: TextView
    private lateinit var tvContactNumber: TextView
    private lateinit var tvLabelTaskDetails: TextView
    private lateinit var tvValueTaskDetails: TextView
    private lateinit var ivImage: ImageView
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_check_in)
        init()

        ivBack.setOnClickListener {
            super.onBackPressed()
        }

        val customerName = intent.getStringExtra("KEY_CUSTOMER_NAME")
        val phone1 = intent.getStringExtra("KEY_PHONE_1")
        val phone2 = intent.getStringExtra("KEY_PHONE_2")
        val address = intent.getStringExtra("KEY_ADDRESS")
        val ticketDescription = intent.getStringExtra("KEY_TICKET_DESCRIPTION")
        val orderInfo = intent.getStringExtra("KEY_ORDER_INFO")
        val ticketId = intent.getStringExtra("KEY_TICKET_ID")
        val latitude = intent.getStringExtra("KEY_LATITUDE")
        val longitude = intent.getStringExtra("KEY_LONGITUDE")
        val imageUrl = intent.getStringExtra("KEY_IMAGE_URL")
        val contactPerson = intent.getStringExtra("KEY_CONTACT_PERSON")

        tvCustomerName.text = customerName
        tvPhone.text = phone1
        tvCustomerAddress.text = address
        tvValueTaskDetails.text = ticketDescription
        tvContactPerson.text = contactPerson
        tvContactNumber.text = phone2

        val ro = RequestOptions()
                .placeholder(R.drawable.ic_image_placeholder)
                .centerCrop()
        Glide.with(this).load(imageUrl).apply(ro).into(ivImage)

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
        ivBack = findViewById(R.id.ivBack)
    }
}
