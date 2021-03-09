package com.teamayka.vansaleandmgmt.components

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import com.teamayka.vansaleandmgmt.ui.purchase.adapters.PurchaseVpAdapter
import com.teamayka.vansaleandmgmt.ui.purchasereturn.adapters.PurchaseReturnVpAdapter
import com.teamayka.vansaleandmgmt.ui.sales.adapters.SalesVpAdapter
import com.teamayka.vansaleandmgmt.ui.salesorder.adapters.SalesOrderVpAdapter
import com.teamayka.vansaleandmgmt.ui.salesreturn.adapters.SalesReturnVpAdapter
import com.teamayka.vansaleandmgmt.utils.TypeManager


class SwipeDisabledViewPager : ViewPager {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (isSwipingEnabled()) {
            super.onTouchEvent(event)
        } else false

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (isSwipingEnabled()) {
            super.onInterceptTouchEvent(event)
        } else false

    }

    private fun isSwipingEnabled(): Boolean {
        if (this.adapter is SalesVpAdapter) {
            val salesFragment = (this.adapter as SalesVpAdapter).currentFragment!!
            if (salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_OLD && salesFragment.isRecyclerViewEnabled) {
                return false
            } else if (salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_FRESH && salesFragment.productItemsList.size > 0)
                return false
        }

        if (this.adapter is SalesOrderVpAdapter) {
            val salesFragment = (this.adapter as SalesOrderVpAdapter).currentFragment!!
            if (salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_OLD && salesFragment.isRecyclerViewEnabled) {
                return false
            } else if (salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_FRESH && salesFragment.productItemsList.size > 0)
                return false
        }

        if (this.adapter is PurchaseVpAdapter) {
            val salesFragment = (this.adapter as PurchaseVpAdapter).currentFragment!!
            if (salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_OLD && salesFragment.isRecyclerViewEnabled) {
                return false
            } else if (salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_FRESH && salesFragment.productItemsList.size > 0)
                return false
        }

        if (this.adapter is SalesReturnVpAdapter) {
            val salesFragment = (this.adapter as SalesReturnVpAdapter).currentFragment!!
            if (salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_OLD && salesFragment.isRecyclerViewEnabled) {
                return false
            } else if (salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_FRESH && salesFragment.productItemsList.size > 0)
                return false
        }

        if (this.adapter is PurchaseReturnVpAdapter) {
            val salesFragment = (this.adapter as PurchaseReturnVpAdapter).currentFragment!!
            if (salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_OLD && salesFragment.isRecyclerViewEnabled) {
                return false
            } else if (salesFragment.invoiceStatus == TypeManager.INVOICE_TYPE_FRESH && salesFragment.productItemsList.size > 0)
                return false
        }

        return true
    }
}
