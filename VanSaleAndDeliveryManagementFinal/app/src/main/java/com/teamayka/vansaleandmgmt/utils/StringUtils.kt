package com.teamayka.vansaleandmgmt.utils

import android.text.TextUtils

object StringUtils {

    private const val DELIMITER = "#*&1%3$)1@)**#@{372&"

    fun serialize(list: ArrayList<String>): String {
        return TextUtils.join(DELIMITER, list)
    }

    fun derialize(content: String): List<String> {
        return content.split(DELIMITER)
    }
}