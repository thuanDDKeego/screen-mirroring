package com.abc.mirroring.ui.dialog

import AdType
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.LayoutDialogAskDisplayOverlayPermissionBinding
import com.abc.mirroring.databinding.LayoutDialogBrowserMirrorBinding
import com.abc.mirroring.databinding.LayoutDialogExitAppBinding
import com.abc.mirroring.databinding.LayoutDialogLoadRewardAdErrorBrowserBinding
import com.abc.mirroring.databinding.LayoutDialogLoadingAdsBinding
import com.abc.mirroring.databinding.LayoutDialogStopOptimizeBatteryBinding
import com.abc.mirroring.databinding.LayoutDialogTooManyAdsBinding
import com.abc.mirroring.databinding.LayoutDialogTutorialFirstOpenBinding
import com.abc.mirroring.databinding.LayoutLoadingBinding
import com.abc.mirroring.databinding.LayoutRateDialogBinding
import com.abc.mirroring.extentions.fadeInAnimation
import com.abc.mirroring.extentions.scaleAnimation
import com.abc.mirroring.helper.requestOverlaysPermission
import com.abc.mirroring.ui.home.HomeActivity
import com.abc.mirroring.ui.home.adapter.TutorialDialogAdapter
import com.abc.mirroring.ui.premium.PremiumActivity
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import one.shot.haki.ads.AdCenter
import timber.log.Timber

class DialogCenter(private val activity: Activity) {
    private val view: ViewGroup = activity.findViewById(android.R.id.content)
    private val layoutInflater = activity.layoutInflater
    lateinit var admobHelper: AdmobHelper
    var mRateDialogShowing = false
    var mLoadingAdsDialogShowing = false
    var mLoadingProgressBarShowing = false
    var tutorialDialogIsShowing = false
    var browserDialogShowing = false
    var browserDialogErrorShowing = false
    var stopOptimizeBatteryDialogShowing = false
    var tooManyAdsDialogShowing = false
    var askPermissionOverLayDialogShowing = false
    var exitAppDialogShowing = false

    private var countDownJob: Job? = null
    private var rewardAdsJob: Job? = null

    private lateinit var dialogRatingBinding: LayoutRateDialogBinding
    private lateinit var dialogLoadingAdBinding: LayoutDialogLoadingAdsBinding
    private lateinit var layoutLoadingBinding: LayoutLoadingBinding

    private lateinit var dialogBrowserBinding: LayoutDialogBrowserMirrorBinding
    private lateinit var dialogBrowserErrorBinding: LayoutDialogLoadRewardAdErrorBrowserBinding
    private lateinit var dialogStopOptimizeBatteryBinding: LayoutDialogStopOptimizeBatteryBinding
    private lateinit var dialogTooManyAdsBinding: LayoutDialogTooManyAdsBinding
    private lateinit var dialogTutorialBinding: LayoutDialogTutorialFirstOpenBinding
    private lateinit var dialogExitAppBinding: LayoutDialogExitAppBinding
    private lateinit var dialogAskPermissionOverLayBinding: LayoutDialogAskDisplayOverlayPermissionBinding

    fun onDestroy() {
        countDownJob?.cancel()
        rewardAdsJob?.cancel()
    }

    private fun resetDialogView() {
        dialogRatingBinding.txtDescription.visibility = View.GONE
        dialogRatingBinding.btnRate.visibility = View.GONE
        dialogRatingBinding.animationEmojis.visibility = View.INVISIBLE
        dialogRatingBinding.imgStar.visibility = View.VISIBLE
//        mRateDialogShowing = false
//        mLoadingAdsDialogShowing = false
//        mLoadingProgressBarShowing = false
    }

    private fun dismissRatingDialog() {
        if (mRateDialogShowing) {
            view.removeViewAt(view.childCount - 1)
            mRateDialogShowing = false
        }
    }

    private fun dismissLoadingAdDialog() {
        if (mLoadingAdsDialogShowing) {
            view.removeViewAt(view.childCount - 1)
            mLoadingAdsDialogShowing = false
        }
    }


    fun dismissLoadingBarDialog() {
        if (mLoadingProgressBarShowing) {
            view.removeViewAt(view.childCount - 1)
            mLoadingProgressBarShowing = false
        }
    }

    private var mAutoShowRating = false
    private fun showRatingDialog(autoShow: Boolean = true, onRate: (Int) -> Unit) {
        if (mRateDialogShowing) return
        mRateDialogShowing = true
        var rating = 5
        val view = activity.findViewById<View>(android.R.id.content) as ViewGroup
        dialogRatingBinding =
            LayoutRateDialogBinding.inflate(activity.layoutInflater, view, true)
        resetDialogView()
        CoroutineScope(Dispatchers.Main).launch {
            for (i in 1..5) {
                delay(250)
                dialogRatingBinding.ratingBarAnimation.rating = i.toFloat()
            }
            dialogRatingBinding.ratingBar.rating = 5f
            delay(150)
            dialogRatingBinding.ratingBarAnimation.visibility = View.GONE
        }
        dialogRatingBinding.btnClose.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Rating_Click_Cancel)
            Timber.d("Rate dialog close $mRateDialogShowing")
            dismissRatingDialog()
        }
        dialogRatingBinding.ratingBar.setOnRatingBarChangeListener { _, _rating, fromUser ->
            rating = _rating.toInt()
            resetDialogView()
            dialogRatingBinding.btnRate.visibility = View.VISIBLE
            dialogRatingBinding.txtDescription.visibility = View.VISIBLE
            dialogRatingBinding.animationEmojis.visibility = View.VISIBLE
            dialogRatingBinding.imgStar.visibility = View.INVISIBLE
            when (rating) {
                0, 1, 2 -> {
                    setUpMessageAndEmoji(
                        getString(R.string.oh_no),
                        getString(R.string.please_leave_us_some_feedback),
                        R.raw.animation_2star
                    )
                }

                3 -> {
                    setUpMessageAndEmoji(
                        getString(R.string.oh_no),
                        getString(R.string.please_leave_us_some_feedback),
                        R.raw.animation_3star
                    )
                }

                4 -> {
                    setUpMessageAndEmoji(
                        getString(R.string.we_like_you_too),
                        getString(R.string.thanks_for_your_feedback),
                        R.raw.animation_4star
                    )
                }

                else -> {
                    Glide.with(activity.applicationContext).load(R.drawable.ic_5stars)
                        .into(dialogRatingBinding.imgStar)
                    setUpMessageAndEmoji(
                        getString(R.string.we_like_you_too),
                        getString(R.string.thanks_for_your_feedback),
                        R.raw.animation_5star
                    )
                }
            }
            dialogRatingBinding.animationEmojis.playAnimation()
        }

        dialogRatingBinding.bgBlackViewInRate.setOnClickListener {
            dismissRatingDialog()
        }

        dialogRatingBinding.mainRatingContentLayout.setOnClickListener {

        }
//        dialogRatingBinding.btnClose.setOnClickListener {
//            dismissRatingDialog()
//        }
        dialogRatingBinding.bgBlackViewInRate.fadeInAnimation()
        dialogRatingBinding.mainRatingContentLayout.scaleAnimation()
        dialogRatingBinding.txtDontAskAgain.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Rating_Click_Dont_Ask_Again)
            dismissRatingDialog()
            AppPreferences().isRated = true
        }
        dialogRatingBinding.btnRate.setOnClickListener {
            AppPreferences().isRated = true
            when (rating) {
                1 -> FirebaseTracking.log(FirebaseLogEvent.Rating_Click_1_Star)
                2 -> FirebaseTracking.log(FirebaseLogEvent.Rating_Click_2_Star)
                3 -> FirebaseTracking.log(FirebaseLogEvent.Rating_Click_3_Star)
                4 -> FirebaseTracking.log(FirebaseLogEvent.Rating_Click_4_Star)
                else -> FirebaseTracking.log(FirebaseLogEvent.Rating_Click_5_Star)
            }
            onRate(rating)
            dismissRatingDialog()
            if (autoShow)
                activity.finishAfterTransition()
        }
    }

    private fun setUpMessageAndEmoji(label: String, message: String, emoij: Int) {
        dialogRatingBinding.layoutRateDialogTitle.text = label
        dialogRatingBinding.txtDescription.text = message
        dialogRatingBinding.animationEmojis.setAnimation(emoij)
    }

    private fun showLoadingAdsDialog() {
        if (mLoadingAdsDialogShowing) return
        mLoadingAdsDialogShowing = true
        val view = activity.findViewById<View>(android.R.id.content) as ViewGroup
        dialogLoadingAdBinding =
            LayoutDialogLoadingAdsBinding.inflate(activity.layoutInflater, view, true)
        val animMoveRightLoadBar = AnimationUtils.loadAnimation(
            activity.applicationContext,
            R.anim.move_right_load_bar
        )
        dialogLoadingAdBinding.viewLoadBar.startAnimation(animMoveRightLoadBar)
        dialogLoadingAdBinding.constraintBgDialogLoadingAd.setOnClickListener {}
    }

    fun showLoadingProgressBar() {
        if (mLoadingProgressBarShowing) return
        mLoadingProgressBarShowing = true
        val view = activity.findViewById<ViewGroup>(android.R.id.content)
        layoutLoadingBinding =
            LayoutLoadingBinding.inflate(activity.layoutInflater, view, true)
    }

    private fun dismissBrowserErrorDialog() {
        if (browserDialogErrorShowing) {
            view.removeViewAt(view.childCount - 1)
            browserDialogErrorShowing = false
        }
    }

    private fun showBrowserErrorDialog(
        label: String?,
        content: String?,
        backgroundId: Int,
        onRewarded: () -> Unit,
        onRetry: () -> Unit
    ) {
        if (browserDialogErrorShowing) return
        browserDialogErrorShowing = true
        dialogBrowserErrorBinding =
            LayoutDialogLoadRewardAdErrorBrowserBinding.inflate(
                activity.layoutInflater,
                view,
                true
            )
        dialogBrowserErrorBinding.apply {
            label?.let { txtTitleDialog.text = it }
            content?.let { txtContentDialog.text = it }
            txtCancel.setOnClickListener {
                dismissBrowserErrorDialog()
            }
            cardDialog.setOnClickListener { }
            constraintBgDialogDisconnect.setOnClickListener { dismissBrowserDialog() }

            txtRetry.setOnClickListener {
                dismissBrowserErrorDialog()
                showBrowserDialog(label, content, backgroundId, onRewarded, onRetry)
            }

        }
    }

    private fun dismissAskingForPremium() {
        if (tooManyAdsDialogShowing) {
            view.removeViewAt(view.childCount - 1)
            tooManyAdsDialogShowing = false
        }
    }

    private fun showAskingForPremium(
        label: String? = null,
        content: String? = null,
        backgroundLabel: Int = R.drawable.bg_dialog_too_many_ads,
        callback: () -> Unit
    ) {
        if (tooManyAdsDialogShowing) return
        tooManyAdsDialogShowing = true
        dialogTooManyAdsBinding =
            LayoutDialogTooManyAdsBinding.inflate(
                activity.layoutInflater,
                view,
                true
            )
        dialogTooManyAdsBinding.apply {
            btnClose.setOnClickListener {
                dismissAskingForPremium()
                callback()
            }
            label?.let { txtTitleDialog.text = label }
            content?.let { txtContentDialog.text = content }
            llBackground.background = ContextCompat.getDrawable(activity, backgroundLabel)
            cardDialog.setOnClickListener { }
            constraintBgTooManyAdsDialog.setOnClickListener { dismissAskingForPremium() }
            btnBuyPremium.setOnClickListener {
                PremiumActivity.gotoActivity(activity)
                dismissAskingForPremium()
                callback()
            }
        }
    }

    private fun dismissBrowserDialog() {
        if (browserDialogShowing) {
            activity.findViewById<ViewGroup>(android.R.id.content)
                .removeViewAt(activity.findViewById<ViewGroup>(android.R.id.content).childCount - 1)
            countDownJob?.cancel()
            countDownJob = null
            rewardAdsJob?.cancel()
            rewardAdsJob = null
            Timber.d("jobState $countDownJob $rewardAdsJob")
            browserDialogShowing = false
        }
    }

    private fun showBrowserDialog(
        label: String?,
        content: String?,
        backgroundId: Int,
        onRewarded: () -> Unit,
        onError: () -> Unit
    ) {
        if (browserDialogShowing) return
        browserDialogShowing = true
        FirebaseTracking.logHomeBrowserDialogShowed()
        dialogBrowserBinding =
            LayoutDialogBrowserMirrorBinding.inflate(layoutInflater, view, true)
        dialogBrowserBinding.apply {
            label?.let {
                txtTitleDialog.visibility = View.VISIBLE
                txtTitleDialog.text = it
            }
            content?.let {
                txtContentDialog.visibility = View.VISIBLE
                txtContentDialog.text = it
            }
            llBackground.background = ContextCompat.getDrawable(activity, backgroundId)
            txtStartVideoInTime.text = getString(R.string.video_starting_in, "5")
            countDownJob = CoroutineScope(Dispatchers.Main).launch {
                for (i in 4 downTo 0) {
                    delay(1000L)
                    txtStartVideoInTime.text = getString(R.string.video_starting_in, i.toString())
                    if (i == 0) {
                        goToRewardAds(onRewarded, onError)
                    }
                }
            }
            llUpgrade.setOnClickListener {
                dismissBrowserDialog()
                countDownJob?.cancel()
                PremiumActivity.gotoActivity(activity)
            }
            btnClose.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Browser_Mirror_Popup_Click_Close)
                dismissBrowserDialog()
            }
            cardDialog.setOnClickListener { }
            constraintBgBrowserDialog.setOnClickListener { dismissBrowserDialog() }

            txtStartVideoInTime.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Browser_Mirror_Popup_Click_Video_Starting)
                countDownJob?.cancel()
                goToRewardAds(onRewarded, onError)
            }
        }
    }

    private fun goToRewardAds(
        onRewarded: () -> Unit,
        onError: () -> Unit
    ) {
        dialogBrowserBinding.apply {
            txtStartVideoInTime.setTextColor(
                ContextCompat.getColor(
                    activity, R.color.txt_disable_gray
                )
            )
            txtStartVideoInTime.setOnClickListener { }
            progressBarLoadAds.visibility = View.VISIBLE
            if (rewardAdsJob == null) {
                rewardAdsJob = CoroutineScope(Dispatchers.Main).launch {
//                    admobHelper.showRewardedAds(
                    AdCenter.getInstance().rewarded?.show(
                        activity
                    ) { isSuccess ->
                        if (isSuccess) {
                            AppPreferences().browserMirroringCountUsages =
                                AppPreferences().browserMirroringCountUsages!! + 1
                            onRewarded()
                            dismissBrowserDialog()
                        } else {
                            dismissBrowserDialog()
                            onError()
//                            showBrowserErrorDialog(
//                                label,
//                                content,
//                                backgroundId,
//                                onRewarded,
//                                onRetry
//                            )
                        }
                    }
                }
            }
        }
    }

    private fun showExitAppDialog() {
        if (exitAppDialogShowing) return
        exitAppDialogShowing = true
        dialogExitAppBinding =
            LayoutDialogExitAppBinding.inflate(layoutInflater, view, true)
        dialogExitAppBinding.apply {

            admobHelper.showNativeAdmob(activity, AdType.EXIT_APP_NATIVE, nativeAdView)

            btnExitApp.setOnClickListener {
//                super.onBackPressed()
                activity.finish()
            }
            btnClose.setOnClickListener {
                dismissExitAppDialog()
            }
            constraintExitAppDialog.setOnClickListener {
                dismissExitAppDialog()
            }

            cardDialog.setOnClickListener { }
        }
    }

    private fun dismissExitAppDialog() {
        if (exitAppDialogShowing) {
            view.removeViewAt(view.childCount - 1)
            exitAppDialogShowing = false
        }
    }

    private fun dismissAskPermissionOverlayDialog() {
        if (askPermissionOverLayDialogShowing) {
            view.removeViewAt(view.childCount - 1)
            askPermissionOverLayDialogShowing = false
        }
    }

    private fun showAskPermissionOverlayDialog() {
        if (askPermissionOverLayDialogShowing) return
        askPermissionOverLayDialogShowing = true
        dialogAskPermissionOverLayBinding =
            LayoutDialogAskDisplayOverlayPermissionBinding.inflate(
                layoutInflater,
                view,
                true
            )
        dialogAskPermissionOverLayBinding.apply {
            btnClose.setOnClickListener { dismissAskPermissionOverlayDialog() }
            btnAllow.setOnClickListener {
                AdCenter.getInstance().appOpen?.disableAddWithActivity(HomeActivity::class.java)
                activity.requestOverlaysPermission()
                dismissAskPermissionOverlayDialog()
            }
            constraintBgDialogAskPermission.setOnClickListener {
                dismissAskPermissionOverlayDialog()
            }
            llDialog.setOnClickListener {}
        }
    }

    private fun updateTabTutorialDialogPager(
        binding: LayoutDialogTutorialFirstOpenBinding,
        position: Int
    ) {
        binding.apply {
            imgStateStep1.setImageResource(R.drawable.ic_state_off_tutorial_dialog)
            imgStateStep2.setImageResource(R.drawable.ic_state_off_tutorial_dialog)
            imgStateStep3.setImageResource(R.drawable.ic_state_off_tutorial_dialog)
            if (position == 0) {
                imgStateStep1.setImageResource(R.drawable.ic_state_on_tutorial_dialog)
            } else if (position == 1) {
                imgStateStep2.setImageResource(R.drawable.ic_state_on_tutorial_dialog)
            } else {
                imgStateStep3.setImageResource(R.drawable.ic_state_on_tutorial_dialog)
            }
            if (position != 0) {
                btnPrevious.visibility = View.VISIBLE
            } else {
                btnPrevious.visibility = View.INVISIBLE
            }
            if (position < 2) {
                btnNext.setOnClickListener {
                    viewPagerTutorialDialog.currentItem = viewPagerTutorialDialog.currentItem + 1
                }
                txtOk.visibility = View.INVISIBLE
                btnNext.visibility = View.VISIBLE
            } else {
                btnNext.visibility = View.INVISIBLE
                txtOk.visibility = View.VISIBLE
            }
        }
    }

    private fun showTutorialDialog(tutorialAdapter: TutorialDialogAdapter) {
        tutorialDialogIsShowing = true
        dialogTutorialBinding =
            LayoutDialogTutorialFirstOpenBinding.inflate(layoutInflater, view, true)
//        val tutorialAdapter = TutorialDialogAdapter(this, supportFragmentManager)
        dialogTutorialBinding.apply {
            viewPagerTutorialDialog.adapter = tutorialAdapter
            constraintBgDialogTutorial.setOnClickListener {}
            updateTabTutorialDialogPager(dialogTutorialBinding, 0)

            viewPagerTutorialDialog.addOnPageChangeListener(object :
                ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                }

                override fun onPageSelected(position: Int) {
                    updateTabTutorialDialogPager(dialogTutorialBinding, position)
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })

            imgStateStep1.setOnClickListener {
                viewPagerTutorialDialog.currentItem = 0
                updateTabTutorialDialogPager(dialogTutorialBinding, 0)
            }

            imgStateStep2.setOnClickListener {
                viewPagerTutorialDialog.currentItem = 1
                updateTabTutorialDialogPager(dialogTutorialBinding, 1)
            }

            imgStateStep3.setOnClickListener {
                viewPagerTutorialDialog.currentItem = 2
                updateTabTutorialDialogPager(dialogTutorialBinding, 2)
            }
            btnPrevious.setOnClickListener {
                viewPagerTutorialDialog.setCurrentItem(
                    viewPagerTutorialDialog.currentItem - 1,
                    true
                )
            }
            txtOk.setOnClickListener {
                if (AppPreferences().isPremiumSubscribed == true) {
                    dismissTutorialDialog()
                } else {
//                    dialogCenter.showLoadingAdsDialog()
                    showDialog(DialogType.LoadingAds)
                    AdCenter.getInstance().interstitial?.show(
                        activity,
                    ) {
                        dismissLoadingAdDialog()
                        dismissTutorialDialog()
                    }
                }
            }
        }
    }

    private fun dismissTutorialDialog() {
        if (tutorialDialogIsShowing) {
            view.removeViewAt(view.childCount - 1)
            tutorialDialogIsShowing = false
        }
    }

    private fun dismissStopOptimizeBatteryDialog() {
        if (stopOptimizeBatteryDialogShowing) {
            view.removeViewAt(view.childCount - 1)
            stopOptimizeBatteryDialogShowing = false
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pwrm =
            context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name)
        }
        return true
    }

    private fun checkBattery() {
        if (!isIgnoringBatteryOptimizations(activity) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val name = getString(R.string.app_name)
//            Toast.makeText(
//                activity.applicationContext,
//                "Battery optimization -> All apps -> $name -> Don't optimize",
//                Toast.LENGTH_LONG
//            ).show()
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
//            intent.data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(intent)
//            Toast.makeText(
//                activity.applicationContext,
//                "Battery optimization1111 -> All apps -> $name -> Don't optimize",
//                Toast.LENGTH_LONG
//            ).show()
        } else {
            Toast.makeText(
                activity.applicationContext,
                "Battery optimization is disabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showStopOptimizeBatteryDialog() {
        if (stopOptimizeBatteryDialogShowing) return
        stopOptimizeBatteryDialogShowing = true
        dialogStopOptimizeBatteryBinding = LayoutDialogStopOptimizeBatteryBinding.inflate(
            layoutInflater,
            view,
            true
        )

        dialogStopOptimizeBatteryBinding.apply {
            txtAllow.setOnClickListener {
                dismissStopOptimizeBatteryDialog()
                checkBattery()
            }

            txtDeny.setOnClickListener {
                dismissStopOptimizeBatteryDialog()
            }
        }
    }

    fun showDialog(type: DialogType) {
        when (type) {
            is DialogType.Rating -> showRatingDialog(type.autoShow, type.onRate)
            is DialogType.LoadingAds -> showLoadingAdsDialog()
            is DialogType.AskingForPremium -> showAskingForPremium(
                type.label,
                type.content,
                type.backgroundId
            ) { type.callback }

            is DialogType.RewardAdNotification -> showBrowserDialog(
                type.label,
                type.content,
                type.backgroundId,
                type.onRewarded,
                type.onError

            )

            is DialogType.StopOptimizeBattery -> showStopOptimizeBatteryDialog()
            is DialogType.RewardAdNotificationError -> showLoadingAdsDialog()
            is DialogType.Tutorial -> showTutorialDialog(type.tutorialAdapter)
            is DialogType.ExitApp -> showExitAppDialog()
            is DialogType.AskPermissionOverLay -> showAskPermissionOverlayDialog()
            else -> {}
        }
    }


    fun dismissDialog(type: DialogType) {
        when (type) {
            is DialogType.Rating -> dismissRatingDialog()
            is DialogType.LoadingAds -> dismissLoadingAdDialog()
            is DialogType.AskingForPremium -> dismissAskingForPremium()
            is DialogType.RewardAdNotification -> dismissBrowserDialog()
            is DialogType.StopOptimizeBattery -> dismissStopOptimizeBatteryDialog()
            is DialogType.RewardAdNotificationError -> dismissBrowserErrorDialog()
            is DialogType.Tutorial -> dismissTutorialDialog()
            is DialogType.ExitApp -> dismissExitAppDialog()
            is DialogType.AskPermissionOverLay -> dismissAskPermissionOverlayDialog()
            else -> {
            }
        }
    }

    sealed interface DialogType {
        data class Rating(val autoShow: Boolean = true, val onRate: (Int) -> Unit) : DialogType
        object LoadingAds : DialogType
        data class AskingForPremium(
            val label: String? = null,
            val content: String? = null,
            val backgroundId: Int = R.drawable.bg_dialog_too_many_ads,
            val callback: () -> Unit
        ) : DialogType

        object StopOptimizeBattery : DialogType
        data class RewardAdNotification(
            val label: String? = null,
            val content: String? = null,
            val backgroundId: Int = R.drawable.bg_dialog_too_many_ads,
            //invoke when reward success
            val onRewarded: () -> Unit,
            val onError: () -> Unit,
        ) : DialogType

        object RewardAdNotificationError : DialogType
        object LoadingBar : DialogType
        data class Tutorial(val tutorialAdapter: TutorialDialogAdapter) : DialogType
        object ExitApp : DialogType
        object AskPermissionOverLay : DialogType
    }

    private fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return activity.getString(resId, *formatArgs)
    }
}