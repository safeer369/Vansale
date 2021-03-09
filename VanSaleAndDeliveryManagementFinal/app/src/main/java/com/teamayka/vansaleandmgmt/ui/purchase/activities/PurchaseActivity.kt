package com.teamayka.vansaleandmgmt.ui.purchase.activities

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.components.SwipeDisabledViewPager
import com.teamayka.vansaleandmgmt.ui.main.models.ProductModel
import com.teamayka.vansaleandmgmt.ui.main.models.SupplierModel
import com.teamayka.vansaleandmgmt.ui.purchase.adapters.PurchaseVpAdapter
import com.teamayka.vansaleandmgmt.ui.purchase.models.PurchaseInvoiceModel
import com.teamayka.vansaleandmgmt.utils.DataCollections
import com.teamayka.vansaleandmgmt.utils.TypeManager

class PurchaseActivity : AppCompatActivity() {

    private lateinit var vp: SwipeDisabledViewPager

    var listInvoices = ArrayList<PurchaseInvoiceModel>()
    var productList = java.util.ArrayList<ProductModel>()
    val productListNames = java.util.ArrayList<String>()

    var customerList = java.util.ArrayList<SupplierModel>()
    val customerListNames = java.util.ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_purchase)
        init()

        productList = DataCollections.getInstance(this).getProducts()
        for (i in 0 until productList.size) {
            productListNames.add(productList[i].displayName)
        }

        customerList = DataCollections.getInstance(this).getSupplier()
        for (i in 0 until customerList.size) {
            customerListNames.add(customerList[i].name)
        }

        listInvoices = DataCollections.getInstance(this).getPurchaseInvoices()
        listInvoices.add(PurchaseInvoiceModel("", "", TypeManager.INVOICE_TYPE_FRESH))

        vp.adapter = PurchaseVpAdapter(supportFragmentManager, listInvoices)
        vp.currentItem = listInvoices.size - 1
    }

    private var isExit = false
    override fun onBackPressed() {
        if (isExit) {
            super.onBackPressed()
        } else {
            // only works with fragment pager adapter not fragment state pager adapter
//            val salesFragment = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.vp + ":" + vp.currentItem) as SalesFragment
            val salesFragment = (vp.adapter as PurchaseVpAdapter).currentFragment!!
            if (salesFragment.productItemsList.size > 0 && salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_FRESH) {
                AlertDialog.Builder(this)
                        .setMessage("Product list will be destroyed. Back?")
                        .setPositiveButton("YES") { p0, p1 ->
                            isExit = true
                            this@PurchaseActivity.onBackPressed()
                        }
                        .setNegativeButton("CANCEL") { p0, p1 -> }
                        .show()
            } else if (salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_OLD && salesFragment.isRecyclerViewEnabled) {
                AlertDialog.Builder(this)
                        .setMessage("You are in edit mode. Back?")
                        .setPositiveButton("YES") { p0, p1 ->
                            isExit = true
                            this@PurchaseActivity.onBackPressed()
                        }
                        .setNegativeButton("CANCEL") { p0, p1 -> }
                        .show()
            } else {
                isExit = true
                super.onBackPressed()
            }
        }
    }

    private fun init() {
        vp = findViewById(R.id.vp)
    }
}
