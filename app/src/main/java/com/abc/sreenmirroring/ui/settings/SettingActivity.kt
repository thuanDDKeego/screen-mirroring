package com.abc.sreenmirroring.ui.settings

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.abc.sreenmirroring.BuildConfig
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.config.AppPreferences
import com.abc.sreenmirroring.databinding.ActivitySettingBinding
import com.abc.sreenmirroring.databinding.LayoutDialogChangePinCodeBinding
import com.abc.sreenmirroring.service.helper.IntentAction
import com.abc.sreenmirroring.ui.tutorial.TutorialActivity

class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, SettingActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivitySettingBinding.inflate(layoutInflater)

    override fun initViews() {
        binding.switchOnOffPinCode.isChecked = AppPreferences().isTurnOnPinCode == true
        binding.txtPinCode.text = AppPreferences().pinCode
    }

    override fun initActions() {
        binding.llHelp.setOnClickListener {
            TutorialActivity.gotoActivity(this@SettingActivity)
        }
        binding.switchOnOffPinCode.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences().isTurnOnPinCode = isChecked
            IntentAction.Exit.sendToAppService(this@SettingActivity)
        }

        binding.llChangePinCode.setOnClickListener {
            IntentAction.Exit.sendToAppService(this@SettingActivity)
            if (AppPreferences().isTurnOnPinCode == true) {
                val dialog =
                    LayoutDialogChangePinCodeBinding.inflate(layoutInflater, binding.root, true)
                dialog.btnSaveNewPinCode.setOnClickListener {
                    AppPreferences().pinCode = dialog.pinView.text.toString()
                    binding.txtPinCode.text = AppPreferences().pinCode
                    dialog.root.visibility = View.INVISIBLE
                    hideKeyboard(this)
                }
                dialog.bgDialog.setOnClickListener {
                    hideKeyboard(this)
                    dialog.root.visibility = View.INVISIBLE
                }
            }
        }
        binding.llRate.setOnClickListener {
            showRatingDialog(false)
        }
        binding.llFeedback.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data =
                Uri.parse("mailto:") // only email apps should handle this

            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("contact@sofigo.net"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
            intent.putExtra(Intent.EXTRA_TEXT, "\n" + getExtraInfo(this))
            if (intent.resolveActivity(this.packageManager) != null) {
                startActivity(intent)
            }
        }
        binding.llInviteFriendItem.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Hey using StoryGo in: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                )
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

    }

    private fun hideKeyboard(context: Context) {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = (context as Activity).currentFocus ?: return
        inputManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun getExtraInfo(context: Context): String {
        val info = StringBuffer("")
        info.append("Extra Info:\n")
        info.append("Model:${Build.MODEL}\n")
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        info.append("Screen size:${width}x${height}\n")
        info.append("Screen density:${Resources.getSystem().displayMetrics.density}\n")

        val actManager: ActivityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalMemory = (memInfo.totalMem / (1024 * 1024 * 1024 * 1024L)).toDouble()
        info.append("Total memory:" + "%.2f".format(totalMemory) + "\n")
        // Declaring MemoryInfo object
        val availMemory = (memInfo.availMem / (1024 * 1024 * 1024 * 1024L)).toDouble()
        info.append("Free memory:" + "%.2f".format(availMemory) + "\n")
        return info.toString()
    }

}