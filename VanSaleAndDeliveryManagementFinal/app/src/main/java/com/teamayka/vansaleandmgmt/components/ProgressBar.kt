package com.teamayka.vansaleandmgmt.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

class ProgressBar : android.support.v7.widget.AppCompatImageView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        // do some tasks here
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        // don't check changedView visibility or visibility param value, both will be invalid. Use this.visibility only
        if (this.visibility == View.VISIBLE)
            doAnimation()
        else
            clearAnimation()
    }

    private fun doAnimation() {
//        val rotateAnimation = RotateAnimation(
//                -15.0f,
//                15.0f,
//                RotateAnimation.RELATIVE_TO_SELF,
//                0.5f,
//                RotateAnimation.RELATIVE_TO_SELF,
//                0.0f)
        val translateAnimation = TranslateAnimation(-20.0f, 20.0f, 0.0f, 0.0f)
        translateAnimation.duration = 300
        translateAnimation.repeatMode = Animation.REVERSE
        translateAnimation.repeatCount = Animation.INFINITE
        translateAnimation.interpolator = AccelerateInterpolator()
        startAnimation(translateAnimation)
    }
}

