package com.abc.mirroring.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.abc.mirroring.notification.TimerReceiver
import java.util.*

/**
 * Created by devdeeds.com on 5/12/17.
 */

class NotificationUtils {


    fun setNotification(timeInMilliSeconds: Long, context: Context) {

        //------------  alarm settings start  -----------------//

        if (timeInMilliSeconds > 0) {


            val alarmManager = context.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(
                context.applicationContext,
                TimerReceiver::class.java
            ) // AlarmReceiver1 = broadcast receiver

            alarmIntent.putExtra("reason", "notification")
            alarmIntent.putExtra("timestamp", timeInMilliSeconds)


            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMilliSeconds


            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        }

        //------------ end of alarm settings  -----------------//


    }
}