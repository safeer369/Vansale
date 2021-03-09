package com.teamayka.vansaleandmgmt.ui.main.adapters

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.teamayka.vansaleandmgmt.R
import com.teamayka.vansaleandmgmt.ui.main.activities.MainActivity

class MainRvAdapter(private val list: ArrayList<String>, private val mainActivity: MainActivity) : RecyclerView.Adapter<MainRvAdapter.ViewHolder>() {

    private var isNeedMeasure = false

    init {
        if (list.size >= 3)
            isNeedMeasure = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_main, parent, false)
        if (isNeedMeasure)
            setMeasuredHeight(v, parent)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setDynamicBackground(position, holder)

        holder.tvLabel.text = list[position]
        mainActivity.setItemIcon(list[position], holder.ivIcon)

        holder.layoutParent.setOnClickListener {
            mainActivity.performItemClick(list[position])
        }
    }

    private fun setDynamicBackground(position: Int, holder: ViewHolder) {
        val mod = position % 4
        if (mod == 0 || mod == 3) {
            holder.layoutParent.setBackgroundColor(ResourcesCompat.getColor(holder.itemView.resources, R.color.colorBgBlueLight, null))
        } else {
            holder.layoutParent.setBackgroundColor(ResourcesCompat.getColor(holder.itemView.resources, R.color.colorBgBlueVeryLight, null))
        }
    }

    private fun setMeasuredHeight(v: View, parent: ViewGroup) {
        val lp = v.layoutParams
        lp.height = parent.measuredHeight / Math.ceil((list.size / 2.0)).toInt() + 1
        v.layoutParams = lp
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutParent: View = itemView.findViewById(R.id.layoutParent)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val tvLabel: TextView = itemView.findViewById(R.id.tvLabel)
    }
}