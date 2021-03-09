package com.teamayka.vansaleandmgmt.ui.filedforce.adapters

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.filedforce.activities.ViewCheckInActivity
import com.teamayka.vansaleandmgmt.ui.filedforce.models.RvCheckInListModel


class RvCheckInListAdapter(private val list: ArrayList<RvCheckInListModel>) : RecyclerView.Adapter<RvCheckInListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_check_in_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.adapterPosition == -1)
            return

        holder.tvSerialNo.text = (position + 1).toString()
        holder.tvServiceType.text = list[position].serviceType
        holder.tvTicketNumber.text = list[position].ticketIDNo
        holder.tvCustomerName.text = list[position].customer
        holder.tvAddress.text = list[position].address

        when (list[position].serviceType) {
            "Pickup" -> {
                holder.tvServiceType.setBackgroundResource(R.drawable.bg_rounded_corner_solid_blue)
            }
            "Delivery" -> {
                holder.tvServiceType.setBackgroundResource(R.drawable.bg_rounded_corner_solid_cyan)
            }
            "Task" -> {
                holder.tvServiceType.setBackgroundResource(R.drawable.bg_rounded_corner_solid_rose)
            }
        }

        holder.layoutParent.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewCheckInActivity::class.java)
            intent.putExtra("KEY_CUSTOMER_NAME", list[holder.adapterPosition].customer)
            intent.putExtra("KEY_PHONE_1", list[holder.adapterPosition].phone1)
            intent.putExtra("KEY_PHONE_2", list[holder.adapterPosition].phone2)
            intent.putExtra("KEY_ADDRESS", list[holder.adapterPosition].address)
            intent.putExtra("KEY_TICKET_DESCRIPTION", list[holder.adapterPosition].ticketDescription)
            intent.putExtra("KEY_ORDER_INFO", list[holder.adapterPosition].orderInfo)
            intent.putExtra("KEY_TICKET_ID", list[holder.adapterPosition].ticketId)
            intent.putExtra("KEY_LATITUDE", list[holder.adapterPosition].latitude)
            intent.putExtra("KEY_LONGITUDE", list[holder.adapterPosition].longitude)
            intent.putExtra("KEY_IMAGE_URL", list[holder.adapterPosition].imageUrl)
            intent.putExtra("KEY_CONTACT_PERSON", list[holder.adapterPosition].contactPerson)
            holder.itemView.context.startActivity(intent)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layoutParent: View = itemView.findViewById(R.id.layoutParent)
        var tvSerialNo: TextView = itemView.findViewById(R.id.tvSerialNo)
        var tvServiceType: TextView = itemView.findViewById(R.id.tvServiceType)
        var tvTicketNumber: TextView = itemView.findViewById(R.id.tvTicketNumber)
        var tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        var tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
    }
}
