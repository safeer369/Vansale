package com.teamayka.vansaleandmgmt.utils

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class RvItemMargin(private val margin: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect?.left = margin
        outRect?.right = margin
        outRect?.top = margin
        outRect?.bottom = margin
    }
}