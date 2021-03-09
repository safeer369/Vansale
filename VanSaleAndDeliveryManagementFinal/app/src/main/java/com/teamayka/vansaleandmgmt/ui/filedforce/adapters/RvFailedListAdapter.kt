package com.teamayka.vansaleandmgmt.ui.filedforce.adapters

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.filedforce.models.RvListModel
import com.teamayka.vansaleandmgmt.ui.filedforce.activities.ViewTicketActivity
import java.util.*


class RvFailedListAdapter(var list: ArrayList<RvListModel>, val date: String) : RecyclerView.Adapter<RvFailedListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_allocated_list, parent, false)
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
        holder.tvServiceName.text = list[position].serviceName
        holder.tvTicketNumber.text = list[position].ticketIDNo
        holder.tvCustomerName.text = list[position].customer
        holder.tvAddress.text = list[position].address
        val time = list[position].time
        if (!TextUtils.isEmpty(time)) {
            holder.tvTime.visibility = View.VISIBLE
            holder.tvTime.text = time
        }

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

        when (list[position].serviceName) {
            "EXP" -> {
                holder.tvServiceName.setBackgroundResource(R.drawable.bg_circle_solid_red)
            }
            "STD" -> {
                holder.tvServiceName.setBackgroundResource(R.drawable.bg_circle_solid_green)
            }
            "QCK" -> {
                holder.tvServiceName.setBackgroundResource(R.drawable.bg_circle_solid_brown)
            }
        }

        holder.layoutParent.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewTicketActivity::class.java)
            intent.putExtra("KEY_CUSTOMER_NAME", list[holder.adapterPosition].customer)
            intent.putExtra("KEY_PHONE_1", list[holder.adapterPosition].phone1)
            intent.putExtra("KEY_PHONE_2", list[holder.adapterPosition].phone2)
            intent.putExtra("KEY_ADDRESS", list[holder.adapterPosition].address)
            intent.putExtra("KEY_TICKET_DESCRIPTION", list[holder.adapterPosition].ticketDescription)
            intent.putExtra("KEY_ORDER_INFO", list[holder.adapterPosition].orderInfo)
            intent.putExtra("KEY_STATUS_ID", list[holder.adapterPosition].statusId)
            intent.putExtra("KEY_TICKET_ID_NO", list[holder.adapterPosition].ticketIDNo)
            intent.putExtra("KEY_TICKET_ID", list[holder.adapterPosition].ticketId)
            intent.putExtra("KEY_LATITUDE", list[holder.adapterPosition].latitude)
            intent.putExtra("KEY_LONGITUDE", list[holder.adapterPosition].longitude)
            intent.putExtra("KEY_DATE", date)
            intent.putExtra("KEY_IS_FAILED", true)
            intent.putExtra("KEY_REMARKS", list[holder.adapterPosition].remarks)
            intent.putExtra("KEY_RECEIVED_BY", list[holder.adapterPosition].receivedBy)
            holder.itemView.context.startActivity(intent)
        }
    }

    fun swapItem(firstPosition: Int, secondPosition: Int) {
        if (firstPosition == -1 || secondPosition == -1)
            return

        if (firstPosition >= list.size || secondPosition >= list.size)
            return

        Collections.swap(list, firstPosition, secondPosition)
        this.notifyItemMoved(firstPosition, secondPosition)
    }

    fun removeItem(position: Int) {
        list.removeAt(position)
        this.notifyItemRemoved(position)
    }

    fun updateList(list: ArrayList<RvListModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layoutParent: View = itemView.findViewById(R.id.layoutParent)
        var tvSerialNo: TextView = itemView.findViewById(R.id.tvSerialNo)
        var tvServiceType: TextView = itemView.findViewById(R.id.tvServiceType)
        var tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        var tvTicketNumber: TextView = itemView.findViewById(R.id.tvTicketNumber)
        var tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        var tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }
}
