package com.teamayka.vansaleandmgmt.ui.stockreport

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import java.util.*

class StockReportRvAdapter(private val list: ArrayList<StockReportModel>) : RecyclerView.Adapter<StockReportRvAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_stock_report, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.adapterPosition == -1)
            return

        holder.tvSerialNo.text = list[position].slNo
        holder.tvGroupName.text = list[position].groupName
        holder.tvValueItemName.text = list[position].itemName
        holder.tvValueBarcode.text = list[position].barcode
        holder.tvValueUnit.text = list[position].unit
        holder.tvValueCategory.text = list[position].category
        holder.tvValueValue.text = list[position].value
        holder.tvValueStock.text = list[position].stock
        holder.tvValueWCP.text = list[position].WCP
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSerialNo: TextView = itemView.findViewById(R.id.tvSerialNo)
        var tvGroupName: TextView = itemView.findViewById(R.id.tvGroupName)
        var tvValueItemName: TextView = itemView.findViewById(R.id.tvValueItemName)
        var tvValueBarcode: TextView = itemView.findViewById(R.id.tvValueBarcode)
        var tvValueUnit: TextView = itemView.findViewById(R.id.tvValueUnit)
        var tvValueCategory: TextView = itemView.findViewById(R.id.tvValueCategory)
        var tvValueValue: TextView = itemView.findViewById(R.id.tvValueValue)
        var tvValueStock: TextView = itemView.findViewById(R.id.tvValueStock)
        var tvValueWCP: TextView = itemView.findViewById(R.id.tvValueWCP)
    }
}