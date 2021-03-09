package com.teamayka.vansaleandmgmt.components

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.widget.TextView

class ExpandableTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : android.support.v7.widget.AppCompatTextView(context, attrs) {

    companion object {
        private const val DEFAULT_TRIM_LENGTH = 100
        private const val ELLIPSIS = "....."
    }

    var originalText: CharSequence? = null
    private var trimmedText: CharSequence? = null
    private var bufferType: TextView.BufferType? = null
    private var trim = true

    var trimLength = DEFAULT_TRIM_LENGTH
        set(trimLength) {
            field = trimLength
            trimmedText = getTrimmedText(originalText)
            setText()
        }

    private val displayableText: CharSequence?
        get() = if (trim) trimmedText else originalText

    init {

        //        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        //        this.trimLength = typedArray.getInt(R.styleable.ExpandableTextView_trimLength, DEFAULT_TRIM_LENGTH);
        //        typedArray.recycle();

        setOnClickListener {
            trim = !trim
            setText()
            requestFocusFromTouch()
        }
    }

    private fun setText() {
        super.setText(displayableText, bufferType)
    }

    override fun setText(text: CharSequence, type: TextView.BufferType) {
        originalText = text
        trimmedText = getTrimmedText(text)
        bufferType = type
        setText()
    }

    private fun getTrimmedText(text: CharSequence?): CharSequence? {
//        for (i in 0 until lineCount){
//
//        }
//        val start = layout.getLineStart(lineCount)
//        val end = layout.getLineEnd(lineCount)
        return if (originalText != null && originalText!!.length > this.trimLength) {
            SpannableStringBuilder(originalText, 0, this.trimLength + 1).append(ELLIPSIS)
        } else {
            originalText
        }
    }
}
