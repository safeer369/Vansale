package com.teamayka.vansaleandmgmt.utils

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

/**
 * Created by User on 24-02-2018.
 */
object PermissionUtils {
    fun hasAllPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.none { ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
    }

    fun hasAllPermissionsGranted(grandResults: IntArray): Boolean {
        return grandResults.none { it != PackageManager.PERMISSION_GRANTED }
    }
}