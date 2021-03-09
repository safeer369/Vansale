package com.teamayka.vansaleandmgmt.utils

import java.text.SimpleDateFormat
import java.util.*

object CalendarUtils {
    fun getCurrentDate(): String {
        val currentDate = Calendar.getInstance().time
        val df = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        return df.format(currentDate)
    }
}