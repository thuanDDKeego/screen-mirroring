package com.abc.mirroring.helper

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.abc.mirroring.R
import com.abc.mirroring.service.helper.IntentAction
import com.abc.mirroring.ui.browsermirror.BrowserMirrorActivity
import com.elvishew.xlog.XLog
import info.dvkr.screenstream.data.model.AppError
import info.dvkr.screenstream.data.model.FixableError
import info.dvkr.screenstream.data.other.getLog

class NotificationHelper(context: Context) {
  companion object {
    private const val CHANNEL_STREAM = "info.dvkr.screenstream.NOTIFICATION_CHANNEL_START_STOP"
    private const val CHANNEL_ERROR = "info.dvkr.screenstream.NOTIFICATION_CHANNEL_ERROR"
  }

  enum class NotificationType(val id: Int) { START(10), STOP(11), ERROR(50) }

  private val applicationContext: Context = context.applicationContext
  private val notificationManager =
    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  private val flagImmutable =
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) 0 else PendingIntent.FLAG_IMMUTABLE

  private var currentNotificationType: NotificationType? = null

  fun createNotificationChannel() {
    currentNotificationType = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationManager.deleteNotificationChannel("info.dvkr.screenstream.service.NOTIFICATION_CHANNEL_01")

      notificationManager.createNotificationChannel(
        NotificationChannel(
          CHANNEL_STREAM,
          "Start/Stop notifications",
          NotificationManager.IMPORTANCE_DEFAULT
        )
          .apply {
            setSound(null, null)
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
          }
      )

      notificationManager.createNotificationChannel(
        NotificationChannel(
          CHANNEL_ERROR,
          "Error notifications",
          NotificationManager.IMPORTANCE_HIGH
        )
          .apply {
            setSound(null, null)
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
          }
      )
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun getNotificationSettingsIntent(): Intent =
    Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
      .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, applicationContext.packageName)

  fun showForegroundNotification(service: Service, notificationType: NotificationType) {
    if (currentNotificationType != notificationType) {
      service.startForeground(
        notificationType.id,
        getForegroundNotification(notificationType)
      )
      currentNotificationType = notificationType
    }
  }

  fun showErrorNotification(appError: AppError) {
    notificationManager.cancel(NotificationType.ERROR.id)

    val message: String = when (appError) {
      is FixableError.AddressInUseException -> "message"
//                applicationContext.getString(R.string.error_port_in_use)
//            is FixableError.CastSecurityException ->
//                applicationContext.getString(R.string.error_invalid_media_projection)
//            is FixableError.AddressNotFoundException ->
//                applicationContext.getString(R.string.error_ip_address_not_found)
//            is FatalError.BitmapFormatException ->
//                applicationContext.getString(R.string.error_wrong_image_format)
      else -> appError.toString()
    }

    val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ERROR)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setCategory(Notification.CATEGORY_ERROR)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setLargeIcon(
        AppCompatResources.getDrawable(applicationContext, R.drawable.logo)?.toBitmap()
      )
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setContentIntent(
        PendingIntent.getActivity(
          applicationContext,
          0,
          BrowserMirrorActivity.getStartIntent(applicationContext),
          flagImmutable
        )
      )

    if (appError is FixableError)
      builder.addAction(
        NotificationCompat.Action(
          null,
          applicationContext.getString(android.R.string.ok),
          PendingIntent.getService(
            applicationContext, 5,
            IntentAction.RecoverError.toAppServiceIntent(applicationContext),
            flagImmutable or PendingIntent.FLAG_UPDATE_CURRENT
          )
        )
      )
    else
      builder.addAction(
        NotificationCompat.Action(
          R.drawable.logo,
          applicationContext.getString(R.string.app_name),
          PendingIntent.getService(
            applicationContext, 5,
            IntentAction.Exit.toAppServiceIntent(applicationContext),
            flagImmutable or PendingIntent.FLAG_UPDATE_CURRENT
          )
        )
      )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ERROR)
      builder
        .setSound(notificationChannel.sound)
        .setPriority(notificationChannel.importance)
        .setVibrate(notificationChannel.vibrationPattern)
    }

    notificationManager.notify(NotificationType.ERROR.id, builder.build())
  }

  fun hideErrorNotification() {
    notificationManager.cancel(NotificationType.ERROR.id)
  }

  private fun getForegroundNotification(notificationType: NotificationType): Notification {
    XLog.d(getLog("getForegroundNotification", "NotificationType: $notificationType"))

    val builder = NotificationCompat.Builder(applicationContext, CHANNEL_STREAM)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setCategory(Notification.CATEGORY_SERVICE)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setLargeIcon(
        AppCompatResources.getDrawable(applicationContext, R.drawable.logo)?.toBitmap()
      )
      .setContentIntent(
        PendingIntent.getActivity(
          applicationContext,
          0,
          BrowserMirrorActivity.getStartIntent(applicationContext),
          flagImmutable
        )
      )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationManager.getNotificationChannel(CHANNEL_STREAM)?.let { notificationChannel ->
        builder
          .setSound(notificationChannel.sound)
          .setPriority(notificationChannel.importance)
          .setVibrate(notificationChannel.vibrationPattern)
      }
    }

    when (notificationType) {
      NotificationType.START -> {
        val builderNotification = builder
          .setContentTitle(applicationContext.getString(R.string.app_name))
          .setContentText("Press START to begin stream")
          .addAction(
            NotificationCompat.Action(
              R.drawable.ic_close,
              "Start",
              PendingIntent.getActivity(
                applicationContext, 1,
                IntentAction.StartStream.toAppActivityIntent(applicationContext),
                flagImmutable or PendingIntent.FLAG_UPDATE_CURRENT
              )
            )
          )
          .addAction(
            NotificationCompat.Action(
              R.drawable.ic_close,
              "Exit",
              PendingIntent.getService(
                applicationContext, 3,
                IntentAction.Exit.toAppServiceIntent(applicationContext),
                flagImmutable or PendingIntent.FLAG_UPDATE_CURRENT
              )
            )
          )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          builderNotification
            .setSmallIcon(R.drawable.ic_launcher_noti).color =
            applicationContext.resources.getColor(R.color.blueA01)
        } else {
          builderNotification.setSmallIcon(R.drawable.ic_launcher_app)
        }

        return builderNotification.build()
      }

      NotificationType.STOP -> {
        val builderNotification = builder
          .setContentTitle(applicationContext.getString(R.string.app_name))
          .setContentText("Press STOP to end stream")
          .addAction(
            NotificationCompat.Action(
              R.drawable.logo,
              "STOP",
              PendingIntent.getService(
                applicationContext, 2,
                IntentAction.StopStream.toAppServiceIntent(applicationContext),
                flagImmutable or PendingIntent.FLAG_UPDATE_CURRENT
              )
            )
          )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          builderNotification
            .setSmallIcon(R.drawable.ic_launcher_noti).color =
            applicationContext.resources.getColor(R.color.blueA01)
        } else {
          builderNotification.setSmallIcon(R.drawable.ic_launcher_app)
        }
        return builderNotification.build()
      }
      else -> throw IllegalArgumentException("Unexpected notification type: $notificationType")
    }
  }
}