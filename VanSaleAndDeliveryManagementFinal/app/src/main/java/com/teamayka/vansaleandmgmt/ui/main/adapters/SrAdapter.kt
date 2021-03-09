package com.teamayka.vansaleandmgmt.ui.main.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.models.CustomerModel

/**
 * Created by User on 14-03-2018.
 */
class SrAdapter(private val mActivity: Activity, private val list: ArrayList<CustomerModel>) : BaseAdapter() {

    var itemList= ArrayList<CustomerModel>()
    init {
        itemList = list
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v = LayoutInflater.from(mActivity).inflate(R.layout.item_sr_customers, parent, false)
        val tvVehicle: TextView = v.findViewById(R.id.tvText)
        tvVehicle.text = list[position].name
        return v
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v = LayoutInflater.from(mActivity).inflate(R.layout.item_sr_customers, parent, false)
        val tvVehicle: TextView = v.findViewById(R.id.tvText)
        tvVehicle.text = list[position].name
        return v
    }

    override fun getItem(position: Int): Any {
        return 0
    }


    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return list.size
    }
}