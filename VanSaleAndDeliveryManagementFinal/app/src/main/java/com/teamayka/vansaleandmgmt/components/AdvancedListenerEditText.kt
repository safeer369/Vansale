package com.teamayka.vansaleandmgmt.components

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText


/**
 * Created by User on 24-01-2018.
 */
class AdvancedListenerEditText : EditText {
    // THIS EDIT TEXT TEXT CHANGE LISTENER DOES NOT WORK WHILE SET TEXT BUT ON CHANGE TEXT MANUALLY
    // NORMALLY ADD TEXT CHANGED LISTENER ADDS MULTIPLE TEXT CHANGED LISTENER BUT IT ONLY SET LAST ONE LISTENER
    var tl: TextWatcher? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun addTextChangedListener(watcher: TextWatcher?) {
        if (tl != null)
            super.removeTextChangedListener(tl)
        super.addTextChangedListener(watcher)
        tl = watcher
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.removeTextChangedListener(tl)
        super.setText(text, type)
        if (tl != null)
            super.addTextChangedListener(tl)
    }

//    fun gettl(): TextWatcher? {
//        return tl
//    }
}