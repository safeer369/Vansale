package com.teamayka.vansaleandmgmt.utils

object MathUtils {
    fun toDouble(value: Any?): Double {
        if (value == null)
            return 0.0
        return try {
            value.toString().toDouble()
        } catch (e: Exception) {
            0.0
        }
    }
}