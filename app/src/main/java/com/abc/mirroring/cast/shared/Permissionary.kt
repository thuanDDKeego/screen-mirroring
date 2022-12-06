package com.abc.mirroring.cast.shared

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dev.sofi.extentions.SofiComponent
import com.abc.mirroring.cast.section.MediaType


@Suppress("SpellCheckingInspection")
class Permissionary {

    /*
    * https://developer.android.com/about/versions/13/behavior-changes-13
    * If your app targets Android 13 or higher and needs to access media files
    *  that other apps have created, you must request one or more of the
    *  following granular media permissions instead of the READ_EXTERNAL_STORAGE permission:
    * Type of media	Permission to request
        Images and photos	READ_MEDIA_IMAGES
        Videos	READ_MEDIA_VIDEO
        Audio files	READ_MEDIA_AUDIO
    * */

    companion object {

        fun getPermission(mediaType: MediaType) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when (mediaType) {
                    MediaType.Audible -> Manifest.permission.READ_MEDIA_VIDEO
                    MediaType.Video -> Manifest.permission.READ_MEDIA_VIDEO
                    MediaType.Audio -> Manifest.permission.READ_MEDIA_AUDIO
                    MediaType.Image -> Manifest.permission.READ_MEDIA_IMAGES
                    else -> Manifest.permission.READ_EXTERNAL_STORAGE /*don't use, Else for youtube, tiktok, etc*/
                }
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

        @OptIn(ExperimentalPermissionsApi::class)
        @Composable
        fun require(
            permission: String = Manifest.permission.READ_EXTERNAL_STORAGE,
            callback: (Boolean) -> Unit = {}
        ) {

            val context = LocalContext.current

            val permissionState = rememberPermissionState(permission = permission, callback)

            val visibility = remember { mutableStateOf(false) }

            if (permissionState.status.isGranted) {
                // if granted, keep the screen going
                callback(true)
                return
            }

            if (!permissionState.status.shouldShowRationale) {
                SideEffect {
                    permissionState.launchPermissionRequest()
                }
            }

            if (permissionState.status.shouldShowRationale) {
                _permission_dialog(
                    visibility = visibility.apply { value = true },
                    description = "Read media permission required for this feature to be available. " +
                            "Please grant the permission",
                    confirmAs = "Grant",
                    onConfirm = { permissionState.launchPermissionRequest() },
                )
            } else {
                _permission_dialog(
                    visibility = visibility.apply { value = true },
                    description = """
                        Read media permission required for this feature to be available. Please grant the permission!
                        But, you have unintentionally denied access multiple times. *sad*
                        To grant permission to the app again, we need you to manually grant permission in the app's settings.
                        Thank you!
                        """.trimIndent(),
                    confirmAs = "Go to Setting",
                    onConfirm = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    },
                )
            }
        }

        private fun checkPermission(context: Context, mediaType: MediaType): Boolean {
            return ContextCompat.checkSelfPermission(context, getPermission(mediaType)) == PackageManager.PERMISSION_GRANTED
        }
    }

}

/**
 * TODO: only use dialog for explain complex permission
 */
@Composable
@SofiComponent(private = true, useFor = ["Permission Requested only"])
internal fun _permission_dialog(
    visibility: MutableState<Boolean>,
    title: String = "Permission required!",
    description: String,
    confirmAs: String,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    if (visibility.value) {
        AlertDialog(
            modifier = Modifier.padding(16.dp),
            onDismissRequest = {
                visibility.value = false
            },
            title = {
                Text(title)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        visibility.value = false
                    },
                ) {
                    Text(confirmAs)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        visibility.value = false
                    },
                ) {
                    Text("Dismiss")
                }
            },
            text = {
                Text(description)
            },
        )
    }
}