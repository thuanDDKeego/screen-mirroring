package com.abc.sreenmirroring.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat


// permission --------------------------------------------------------------------------------------

/**
 * by default, display over other app permission will be granted automatically if minor than android M
 *
 * - some MIUI devices may not work properly
 *
 * */
fun Context.isDrawOverlaysPermissionGranted(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return true
    }

    return Settings.canDrawOverlays(this)
}

fun Context.requestOverlaysPermission() {
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    intent.data = Uri.fromParts("package", packageName, null)
    ContextCompat.startActivity(this, intent, null)
}