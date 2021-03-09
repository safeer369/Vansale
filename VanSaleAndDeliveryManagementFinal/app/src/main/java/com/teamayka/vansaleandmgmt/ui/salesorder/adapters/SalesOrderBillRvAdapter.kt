package com.teamayka.vansaleandmgmt.ui.salesorder.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.models.ProductModel
import com.teamayka.vansaleandmgmt.utils.DataCollections
import java.util.*

class SalesOrderBillRvAdapter(private val list: ArrayList<ProductModel>) : RecyclerView.Adapter<SalesOrderBillRvAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_sales_order_bill, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.adapterPosition == -1)
            return

        holder.tvSerialNo.text = "%s".format(position + 1)
        holder.tvValueItemName.text = "%s".format(list[position].productName)
        holder.tvValueBarcode.text = "%s".format(list[position].productCode)
        holder.tvValueUnit.text = "%s".format(list[position].unit)
        holder.tvValueRate.text = ("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(list[position].rate1)
        holder.tvValueQuantity.text = ("%.3f").format(list[position].quantity)
        holder.tvValueTax.text = ("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(list[position].taxAmount)
        holder.tvValueDiscount.text = ("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(list[position].discountAmount)
        holder.tvValueTotal.text = ("%." + DataCollections.getInstance(holder.itemView.context).getUser()!!.amountType + "f").format(list[position].grandTotal)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSerialNo: TextView = itemView.findViewById(R.id.tvSerialNo)
        var tvValueItemName: TextView = itemView.findViewById(R.id.tvValueItemName)
        var tvValueBarcode: TextView = itemView.findViewById(R.id.tvValueBarcode)
        var tvValueUnit: TextView = itemView.findViewById(R.id.tvValueUnit)
        var tvValueRate: TextView = itemView.findViewById(R.id.tvValueRate)
        var tvValueQuantity: TextView = itemView.findViewById(R.id.tvValueQuantity)
        var tvValueTax: TextView = itemView.findViewById(R.id.tvValueTax)
        var tvValueDiscount: TextView = itemView.findViewById(R.id.tvValueDiscount)
        var tvValueTotal: TextView = itemView.findViewById(R.id.tvValueTotal)
    }
}