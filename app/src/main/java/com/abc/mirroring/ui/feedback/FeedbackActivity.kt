package com.abc.mirroring.ui.feedback

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.databinding.ActivityFeedBackBinding

class FeedbackActivity : BaseActivity<ActivityFeedBackBinding>() {
    companion object {
        fun newIntent(activity: Activity) {
            val intent = Intent(activity, FeedbackActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivityFeedBackBinding.inflate(layoutInflater)

    override fun initViews() {

    }

    override fun initActions() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnSend.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SENDTO)
            sendIntent.data =
                Uri.parse("mailto:") // only email apps should handle this

            sendIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("contact@sofigo.net"))
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback to Screen Mirroring")
            sendIntent.putExtra(Intent.EXTRA_TEXT, "\n" + getExtraInfo(this))
            startActivity(sendIntent)
        }
    }

    private fun getExtraInfo(context: Context): String {
        val des = binding.txtDescription.text
        val info = StringBuffer("")
        info.append("Description:\n${des}")

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