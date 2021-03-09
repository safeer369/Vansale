package com.teamayka.vansaleandmgmt.components

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class RatioTextView : TextView {

    companion object {
        const val HEIGHT = 1
        const val WIDTH = 2
    }

    private var ratio: Double = 0.0
    private var ratioBy = WIDTH // BASED ON

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (ratio == 0.0) {
            when (ratioBy) {
                HEIGHT -> super.onMeasure(heightMeasureSpec, heightMeasureSpec)
                WIDTH -> super.onMeasure(widthMeasureSpec, widthMeasureSpec)
            }
            return
        } else {
            when (ratioBy) {
                HEIGHT -> {
                    val calculatedWidth = calculateWidth(heightMeasureSpec)
                    super.onMeasure(calculatedWidth, heightMeasureSpec)
                }
                WIDTH -> {
                    val calculatedHeight = calculateHeight(widthMeasureSpec)
                    super.onMeasure(widthMeasureSpec, calculatedHeight)
                }
            }
        }
    }

    private fun calculateHeight(widthMeasureSpec: Int): Int {
        return MeasureSpec.getMode(widthMeasureSpec) + (MeasureSpec.getSize(widthMeasureSpec).toDouble() / this.ratio).toInt()
    }

    private fun calculateWidth(heightMeasureSpec: Int): Int {
        return MeasureSpec.getMode(heightMeasureSpec) + (MeasureSpec.getSize(heightMeasureSpec).toDouble() / this.ratio).toInt()
    }

    fun setRatio(ratio: Double, ratioBy: Int) {
        this.ratio = ratio
        this.ratioBy = ratioBy
        requestLayout()
        invalidate()
    }
}
