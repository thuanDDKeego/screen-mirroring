package com.abc.mirroring.ui.settings

import AdType
import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import com.abc.mirroring.BuildConfig
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivitySettingBinding
import com.abc.mirroring.databinding.LayoutDialogChangePinCodeBinding
import com.abc.mirroring.extentions.setTintColor
import com.abc.mirroring.service.ServiceMessage
import com.abc.mirroring.service.helper.IntentAction
import com.abc.mirroring.ui.browsermirror.PermissionActivity
import com.abc.mirroring.ui.browsermirror.StreamViewModel
import com.abc.mirroring.ui.dialog.DialogCenter
import com.abc.mirroring.ui.feedback.FeedbackActivity
import com.abc.mirroring.ui.home.HomeActivity
import com.abc.mirroring.ui.policy.PolicyActivity
import com.abc.mirroring.ui.premium.PremiumActivity
import com.abc.mirroring.ui.selectLanguage.SelectLanguageActivity
import com.abc.mirroring.ui.tutorial.TutorialActivity
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    private val isTurnOnPinCode = MutableLiveData(AppPreferences().isTurnOnPinCode == true)
    private var shakeAnimJob: Job? = null
    private lateinit var dialogCenter: DialogCenter

    @Inject
    lateinit var admobHelper: AdmobHelper

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, SettingActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivitySettingBinding.inflate(layoutInflater)

    override fun initViews() {
        if (AppPreferences().isPremiumSubscribed == true) {
            binding.btnBuyPremium.visibility = View.GONE
            binding.llBanner.visibility = View.GONE
        } else {
            binding.btnBuyPremium.visibility = View.VISIBLE
            binding.llBanner.visibility = View.VISIBLE
        }

        dialogCenter = DialogCenter(this)
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
        binding.txtVersioncode.text = BuildConfig.VERSION_NAME
        binding.btnBuyPremium.visibility =
            if (AppConfigRemote().enable_premium == true) View.VISIBLE else View.GONE
    }

    override fun initAdmob() {
        super.initAdmob()
        admobHelper.showNativeAdmob(
            this@SettingActivity,
            AdType.HOME_NATIVE,
            binding.nativeAdView.nativeAdView,
            true
        )
    }

    override fun initActions() {
        binding.btnBuyPremium.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Setting_Click_Subscription)
            PremiumActivity.gotoActivity(this@SettingActivity)
        }
        binding.llBanner.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Setting_Click_Xmas_Banner)
            PremiumActivity.gotoActivity(this@SettingActivity)
        }
        binding.llHelp.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Setting_Click_Help)
            TutorialActivity.gotoActivity(this@SettingActivity)
        }
        binding.switchOnOffPinCode.setOnCheckedChangeListener { _, isChecked ->
            FirebaseTracking.log(FirebaseLogEvent.Setting_Click_Pin_Switch)
            AppPreferences().isTurnOnPinCode = isChecked
            HomeActivity.isStreamingBrowser.value = false
            isTurnOnPinCode.value = isChecked
            PermissionActivity.settings?.enablePin = isChecked
            val serviceMessage = StreamViewModel.getInstance().serviceMessageLiveData.value
            if (serviceMessage is ServiceMessage.ServiceState && serviceMessage.isStreaming) {
                IntentAction.StopStream.sendToAppService(this@SettingActivity)
            }
        }

        binding.llChangePinCode.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Setting_Click_Change_Pin_Code)
            if (AppPreferences().isTurnOnPinCode == true) {
                val dialog =
                    LayoutDialogChangePinCodeBinding.inflate(layoutInflater, binding.root, true)
                showKeyboard()
                dialog.pinView.doOnTextChanged { text, _, _, _ ->
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
                dialog.pinView.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        if (dialog.pinView.text?.length == 4) {
                            hideKeyboard()
                            HomeActivity.isStreamingBrowser.value = false
                            AppPreferences().pinCode = dialog.pinView.text.toString()
                            binding.txtPinCode.text = AppPreferences().pinCode
                            dialog.root.visibility = View.INVISIBLE
                            PermissionActivity.settings?.pin = AppPreferences().pinCode!!
//                            IntentAction.Exit.sendToAppService(this@SettingActivity)
                            val serviceMessage =
                                StreamViewModel.getInstance().serviceMessageLiveData.value
                            if (serviceMessage is ServiceMessage.ServiceState && serviceMessage.isStreaming) {
                                IntentAction.StopStream.sendToAppService(this@SettingActivity)
                            }
                        }
                        true
                    } else {
                        false
                    }
                }
                dialog.btnSaveNewPinCode.setOnClickListener {
                    hideKeyboard()
                    AppPreferences().pinCode = dialog.pinView.text.toString()
                    binding.txtPinCode.text = AppPreferences().pinCode
                    HomeActivity.isStreamingBrowser.value = false
                    dialog.root.visibility = View.INVISIBLE
                    PermissionActivity.settings?.pin = AppPreferences().pinCode!!
//                    IntentAction.Exit.sendToAppService(this@SettingActivity)
                    val serviceMessage = StreamViewModel.getInstance().serviceMessageLiveData.value
                    if (serviceMessage is ServiceMessage.ServiceState && serviceMessage.isStreaming) {
                        IntentAction.StopStream.sendToAppService(this@SettingActivity)
                    }
                }
                dialog.cardDialog.setOnClickListener {
                    hideKeyboard()
                }
                dialog.bgDialog.setOnClickListener {
                    hideKeyboard()
                    dialog.root.visibility = View.INVISIBLE
                }
                dialog.btnClose.setOnClickListener {
                    hideKeyboard()
                    dialog.root.visibility = View.INVISIBLE
                }
            }
        }

        binding.llRate.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Setting_Click_Rate)
            dialogCenter.showDialog(DialogCenter.DialogType.Rating(false) { star ->
                if (star <= 3) {
                    FeedbackActivity.start(this, star)
                } else {
                    openAppInStore()
                }
            })
//            dialogCenter.showRatingDialog(false) { star ->
//                if (star <= 3) {
//                    FeedbackActivity.start(this, star)
//                } else {
//                    openAppInStore()
//                }
//            }
        }
        binding.llLanguage.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Setting_Click_Language)
            startActivity(SelectLanguageActivity.newIntent(this))
        }
        binding.llFeedback.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Setting_Click_Feedback)
            FeedbackActivity.start(this)
        }
        binding.llInviteFriendItem.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Setting_Click_Invite_Friends)
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
            FirebaseTracking.log(FirebaseLogEvent.Setting_Click_Policy)
            PolicyActivity.gotoActivity(this@SettingActivity)
        }
        binding.btnBack.setOnClickListener {

            finish()
        }
    }

    private fun showKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyboard() {
        val inputManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val v = currentFocus ?: return
        inputManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        if (AppPreferences().isPremiumSubscribed == true) {
            binding.btnBuyPremium.visibility = View.GONE
            binding.llBanner.visibility = View.GONE
        } else {
            val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
            shakeAnimJob = CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    delay(1000L)
                    withContext(Dispatchers.Main) {
                        binding.btnBuyPremium.clearAnimation()
                        binding.btnBuyPremium.startAnimation(shake)
                    }
                    delay(9000L)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (dialogCenter.mRateDialogShowing) {
            dialogCenter.dismissDialog(DialogCenter.DialogType.Rating {})
            return
        }
        super.onBackPressed()
    }

    override fun onStop() {
        super.onStop()
        shakeAnimJob?.cancel()
        shakeAnimJob = null
    }
}