package com.abc.mirroring.cast.shared.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import net.sofigo.cast.tv.R

object FeedbackUtils {
    fun sendFeedback(context: Context, content: String = "") {
        Intent(Intent.ACTION_SENDTO)
            .apply {
                data = Uri.parse("mailto:") // only email apps should handle this

                putExtra(Intent.EXTRA_EMAIL, arrayOf("contact@sofigo.net"))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback to ${context.getString(R.string.app_name)}")
                // TODO: we should append user input content
                 putExtra(Intent.EXTRA_TEXT, content + "\n" + getExtraInfo(context))
            }.run {
                context.startActivity(this)
            }
    }

    private fun getExtraInfo(context: Context): String {
        val info = StringBuffer("")
        info.append("\nExtra Info:\n")
        info.append("Model:${Build.MODEL}\n")
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        info.append("Screen size:${width}x${height}\n")
        info.append("Screen density:${Resources.getSystem().displayMetrics.density}\n")

        val actManager: ActivityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val totalMemory = memInfo.totalMem / (1024 * 1024 * 1024L).toDouble()
        info.append("Total memory:${String.format("%.2f", totalMemory)}gb\n")
        // Declaring MemoryInfo object
        val availMemory = (memInfo.availMem / (1024 * 1024 * 1024L)).toDouble()
        info.append("Free memory:${String.format("%.2f", availMemory)}gb\n")
        return info.toString()
    }
}