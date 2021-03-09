package com.teamayka.vansaleandmgmt.ui.purchasereturn.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.models.ProductModel
import com.teamayka.vansaleandmgmt.ui.purchase.adapters.PurchaseBillRvAdapter
import com.teamayka.vansaleandmgmt.utils.RvItemMargin

class PurchaseReturnBillActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_FINISH_ACTIVITIES = 1001
    }

    private lateinit var fabFinish: FloatingActionButton
    private lateinit var fabBack: FloatingActionButton
    private lateinit var rvBill: RecyclerView
    private lateinit var tvValueDate: TextView
    private lateinit var tvValueName: TextView
    private lateinit var tvValueAddress: TextView
    private lateinit var tvValuePhone: TextView
    private lateinit var tvValueTotal: TextView
    private lateinit var tvValueTaxAmount: TextView
    private lateinit var tvValueDiscount: TextView
    private lateinit var tvValueNetTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_return_bill)
        init()

        val list = intent.extras.getParcelableArrayList<ProductModel>("KEY_LIST")
        rvBill.addItemDecoration(RvItemMargin(10))
        rvBill.adapter = PurchaseBillRvAdapter(list)

        val isUpdate = intent.getBooleanExtra("KEY_IS_UPDATE", false)
        val invoiceId = intent.getStringExtra("KEY_INVOICE_ID")
        tvValueDate.text = intent.getStringExtra("KEY_DATE")
        val customerId = intent.getStringExtra("KEY_CUSTOMER_ID")
        tvValueName.text = intent.getStringExtra("KEY_NAME")
        tvValueAddress.text = intent.getStringExtra("KEY_ADDRESS")
        tvValuePhone.text = intent.getStringExtra("KEY_PHONE")
        tvValueTotal.text = intent.getStringExtra("KEY_TOTAL")
        tvValueTaxAmount.text = intent.getStringExtra("KEY_TAX_AMOUNT")
        tvValueDiscount.text = intent.getStringExtra("KEY_DISCOUNT_AMOUNT")
        tvValueNetTotal.text = intent.getStringExtra("KEY_NET_TOTAL")
        val specialDiscount = intent.getStringExtra("KEY_SPECIAL_DISCOUNT_AMOUNT")
        val referenceNo = intent.getStringExtra("KEY_REFERENCE_NUMBER")

        fabBack.setOnClickListener {
            finish()
        }

        fabFinish.setOnClickListener {
            val intent = Intent(this, PurchaseReturnFinishActivity::class.java)
            intent.putExtra("KEY_IS_UPDATE", isUpdate)
            intent.putExtra("KEY_INVOICE_ID", invoiceId)
            intent.putParcelableArrayListExtra("KEY_LIST", list)
            intent.putExtra("KEY_DATE", tvValueDate.text.toString())
            intent.putExtra("KEY_CUSTOMER_ID", customerId)
            intent.putExtra("KEY_NAME", tvValueName.text.toString())
            intent.putExtra("KEY_ADDRESS", tvValueAddress.text.toString())
            intent.putExtra("KEY_PHONE", tvValuePhone.text.toString())
            intent.putExtra("KEY_TOTAL", tvValueTotal.text.toString())
            intent.putExtra("KEY_TAX_AMOUNT", tvValueTaxAmount.text.toString())
            intent.putExtra("KEY_DISCOUNT_AMOUNT", tvValueDiscount.text.toString())
            intent.putExtra("KEY_NET_TOTAL", tvValueNetTotal.text.toString())
            intent.putExtra("KEY_SPECIAL_DISCOUNT_AMOUNT", specialDiscount)
            intent.putExtra("KEY_REFERENCE_NUMBER",referenceNo )
            startActivityForResult(intent, REQUEST_CODE_FINISH_ACTIVITIES)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_FINISH_ACTIVITIES -> {
                if (resultCode == Activity.RESULT_OK) {
                    //  finishes SalesBillActivity and SalesActivity(if parent) OR SalesInvoiceEditActivity(if parent)
                    finishAffinity()
                }
            }
        }
    }

    private fun init() {
        fabBack = findViewById(R.id.fabBack)
        fabFinish = findViewById(R.id.fabFinish)
        rvBill = findViewById(R.id.rvBill)
        tvValueDate = findViewById(R.id.tvValueDate)
        tvValueName = findViewById(R.id.tvValueName)
        tvValueAddress = findViewById(R.id.tvValueAddress)
        tvValuePhone = findViewById(R.id.tvValuePhone)
        tvValueTotal = findViewById(R.id.tvValueTotal)
        tvValueTaxAmount = findViewById(R.id.tvValueTaxAmount)
        tvValueDiscount = findViewById(R.id.tvValueDiscount)
        tvValueNetTotal = findViewById(R.id.tvValueNetTotal)
    }
}
