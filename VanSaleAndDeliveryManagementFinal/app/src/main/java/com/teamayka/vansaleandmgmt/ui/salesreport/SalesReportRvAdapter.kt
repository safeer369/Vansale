package com.teamayka.vansaleandmgmt.ui.salesreport

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import java.util.*

class SalesReportRvAdapter(private val list: ArrayList<SalesReportModel>) : RecyclerView.Adapter<SalesReportRvAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_sales_report, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.adapterPosition == -1)
            return

        holder.tvSerialNo.text = list[position].slNo
        holder.tvCustomerName.text = list[position].customerName
        holder.tvValueInvoiceNo.text = list[position].invoiceNo
        holder.tvValueDate.text = list[position].invoiceDate
        holder.tvValueGrossAmount.text = list[position].grossAmount
        holder.tvValueDiscountAmount.text = list[position].discount
        holder.tvValueTotal.text = list[position].netAmount
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSerialNo: TextView = itemView.findViewById(R.id.tvSerialNo)
        var tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        var tvValueInvoiceNo: TextView = itemView.findViewById(R.id.tvValueInvoiceNo)
        var tvValueDate: TextView = itemView.findViewById(R.id.tvValueDate)
        var tvValueGrossAmount: TextView = itemView.findViewById(R.id.tvValueGrossAmount)
        var tvValueDiscountAmount: TextView = itemView.findViewById(R.id.tvValueDiscountAmount)
        var tvValueTotal: TextView = itemView.findViewById(R.id.tvValueTotal)
    }
}