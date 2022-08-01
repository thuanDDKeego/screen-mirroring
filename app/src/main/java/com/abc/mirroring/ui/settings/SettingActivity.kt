package com.abc.mirroring.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import com.abc.mirroring.BuildConfig
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivitySettingBinding
import com.abc.mirroring.databinding.LayoutDialogChangePinCodeBinding
import com.abc.mirroring.extentions.setTintColor
import com.abc.mirroring.service.helper.IntentAction
import com.abc.mirroring.ui.feedback.FeedbackActivity
import com.abc.mirroring.ui.policy.PolicyActivity
import com.abc.mirroring.ui.selectLanguage.SelectLanguageActivity
import com.abc.mirroring.ui.tutorial.TutorialActivity
import com.abc.mirroring.utils.FirebaseTracking
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    @Inject
    lateinit var admobHelper: AdmobHelper
    private val isTurnOnPinCode = MutableLiveData(AppPreferences().isTurnOnPinCode == true)

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, SettingActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivitySettingBinding.inflate(layoutInflater)

    override fun initViews() {
        FirebaseTracking.logSettingShowed()
        binding.switchOnOffPinCode.isChecked = AppPreferences().isTurnOnPinCode == true
        isTurnOnPinCode.observe(this) {
            binding.imgChangePinCode.setTintColor(if (it) R.color.blueA01 else R.color.time_second_float)
            binding.txtPinCode.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (it) R.color.txt_gray01 else R.color.time_second_float
                )
            )
            binding.txtChangePinCode.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (it) R.color.txt_black else R.color.time_second_float
                )
            )
        }
        binding.txtPinCode.text = AppPreferences().pinCode
        binding.txtLanguage.text = dLocale?.displayName
    }

    override fun initAdmob() {
        admobHelper.loadAdBanner(binding.adBannerSetting.adView)
    }

    override fun initActions() {
        binding.llHelp.setOnClickListener {
            TutorialActivity.gotoActivity(this@SettingActivity)
        }
        binding.switchOnOffPinCode.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences().isTurnOnPinCode = isChecked
            isTurnOnPinCode.value = isChecked
            IntentAction.Exit.sendToAppService(this@SettingActivity)
        }

        binding.llChangePinCode.setOnClickListener {
            if (AppPreferences().isTurnOnPinCode == true) {
                val dialog =
                    LayoutDialogChangePinCodeBinding.inflate(layoutInflater, binding.root, true)
                showKeyboard(this)
                dialog.pinView.doOnTextChanged { text, start, before, count ->
                    if (text?.length == 4) {
                        dialog.btnSaveNewPinCode.isEnabled = true
                        dialog.btnSaveNewPinCode.backgroundTintList =
                            this.getColorStateList(R.color.blueA01)
                    } else {
                        dialog.btnSaveNewPinCode.isEnabled = false
                        dialog.btnSaveNewPinCode.backgroundTintList =
                            this.getColorStateList(R.color.grayA01)
                    }
                }
                dialog.btnSaveNewPinCode.setOnClickListener {
                    hideKeyboard(this)
                    AppPreferences().pinCode = dialog.pinView.text.toString()
                    binding.txtPinCode.text = AppPreferences().pinCode
                    dialog.root.visibility = View.INVISIBLE
                    IntentAction.Exit.sendToAppService(this@SettingActivity)
                }
                dialog.cardDialog.setOnClickListener {
                    hideKeyboard(this)
                }
                dialog.bgDialog.setOnClickListener {
                    hideKeyboard(this)
                    dialog.root.visibility = View.INVISIBLE
                }
                dialog.btnClose.setOnClickListener {
                    hideKeyboard(this)
                    dialog.root.visibility = View.INVISIBLE
                }
            }
        }
        binding.llRate.setOnClickListener {
            showRatingDialog(false)
        }
        binding.llLanguage.setOnClickListener {
            startActivity(SelectLanguageActivity.newIntent(this))
        }
        binding.llFeedback.setOnClickListener {
            FeedbackActivity.newIntent(this)
        }
        binding.llInviteFriendItem.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Hey using ${getString(R.string.app_name)} in: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                )
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        binding.llPolicy.setOnClickListener {
            PolicyActivity.gotoActivity(this@SettingActivity)
        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

    }

    private fun showKeyboard(context: Context) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyboard(context: Context) {
        val inputManager =
            context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val v = (context as Activity).currentFocus ?: return
        inputManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

}