package com.teamayka.vansaleandmgmt.ui.purchase.adapters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup
import com.teamayka.vansaleandmgmt.ui.purchase.fragments.PurchaseFragment
import com.teamayka.vansaleandmgmt.ui.purchase.models.PurchaseInvoiceModel
import com.teamayka.vansaleandmgmt.ui.sales.fragments.SalesFragment
import com.teamayka.vansaleandmgmt.ui.sales.models.SalesInvoiceModel

/**
 * Created by seydbasil on 3/29/2018.
 */
class PurchaseVpAdapter(fm: FragmentManager, private val list: ArrayList<PurchaseInvoiceModel>) : FragmentPagerAdapter(fm) {
    var currentFragment: PurchaseFragment? = null

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getItem(position: Int): Fragment {
        val invoice = list[position]
        val fragment = PurchaseFragment()
        val bundle = Bundle()
        bundle.putInt("KEY_POSITION", position)
        bundle.putString("KEY_INVOICE_ID", invoice.invoiceId)
        bundle.putString("KEY_INVOICE_NO", invoice.invoiceNo)
        bundle.putString("KEY_INVOICE_STATUS", invoice.invoiceStatus)
        fragment.arguments = bundle
        return fragment
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if (currentFragment != `object`) {
            currentFragment = `object` as PurchaseFragment
        }
        super.setPrimaryItem(container, position, `object`)
    }
}