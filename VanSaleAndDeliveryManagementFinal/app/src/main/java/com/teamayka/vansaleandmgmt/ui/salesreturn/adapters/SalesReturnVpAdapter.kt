package com.teamayka.vansaleandmgmt.ui.salesreturn.adapters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup
import com.teamayka.vansaleandmgmt.ui.salesreturn.fragments.SalesReturnFragment
import com.teamayka.vansaleandmgmt.ui.salesreturn.models.SalesReturnInvoiceModel

/**
 * Created by seydbasil on 3/29/2018.
 */
class SalesReturnVpAdapter(fm: FragmentManager, private val list: ArrayList<SalesReturnInvoiceModel>) : FragmentPagerAdapter(fm) {
    var currentFragment: SalesReturnFragment? = null

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getItem(position: Int): Fragment {
        val invoice = list[position]
        val fragment = SalesReturnFragment()
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
            currentFragment = `object` as SalesReturnFragment
        }
        super.setPrimaryItem(container, position, `object`)
    }
}