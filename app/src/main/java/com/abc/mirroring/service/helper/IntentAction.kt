package com.abc.mirroring.service.helper

import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import com.abc.mirroring.service.AppService
import com.abc.mirroring.ui.browsermirror.BrowserMirrorActivity
import kotlinx.parcelize.Parcelize
import timber.log.Timber

sealed class IntentAction : Parcelable {
    internal companion object {
        private const val EXTRA_PARCELABLE = "EXTRA_PARCELABLE"
        fun fromIntent(intent: Intent?): IntentAction? =
            intent?.getParcelableExtra(EXTRA_PARCELABLE)
    }

    fun toAppServiceIntent(context: Context): Intent =
        AppService.getAppServiceIntent(context).apply {
            putExtra(EXTRA_PARCELABLE, this@IntentAction)
        }

    fun toAppActivityIntent(context: Context): Intent =
        BrowserMirrorActivity.getAppActivityIntent(context).apply {
            putExtra(EXTRA_PARCELABLE, this@IntentAction)
        }

    fun sendToAppService(context: Context) {
        Timber.d("sendToAppService ${this is StartOnBoot}")
        if (this is StartOnBoot)
            AppService.startForeground(context, this.toAppServiceIntent(context))
        else
            AppService.startService(context, this.toAppServiceIntent(context))
    }

    override fun toString(): String = javaClass.simpleName

    override fun writeToParcel(parcel: Parcel, flags: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

    @Parcelize
    object GetServiceState : IntentAction()
    @Parcelize
    object StartStream : IntentAction()
    @Parcelize
    object StopStream : IntentAction()
    @Parcelize
    object Exit : IntentAction()
    @Parcelize
    data class CastIntent(val intent: Intent) : IntentAction()
    @Parcelize
    object CastPermissionsDenied : IntentAction()
    @Parcelize
    object StartOnBoot : IntentAction()
    @Parcelize
    object RecoverError : IntentAction()
}