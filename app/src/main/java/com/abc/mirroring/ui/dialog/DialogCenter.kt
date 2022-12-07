package com.abc.mirroring.ui.dialog

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.abc.mirroring.R
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.LayoutDialogLoadingAdsBinding
import com.abc.mirroring.databinding.LayoutLoadingBinding
import com.abc.mirroring.databinding.LayoutRateDialogBinding
import com.abc.mirroring.extentions.fadeInAnimation
import com.abc.mirroring.extentions.scaleAnimation
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DialogCenter(private val activity: Activity) {
    var mRateDialogShowing = false
    var mLoadingAdsDialogShowing = false
    var mLoadingProgressBarShowing = false

    private lateinit var dialogRatingBinding: LayoutRateDialogBinding
    private lateinit var dialogLoadingAdBinding: LayoutDialogLoadingAdsBinding
    private lateinit var layoutLoadingBinding: LayoutLoadingBinding

    private fun resetDialogView() {
        dialogRatingBinding.txtDescription.visibility = View.GONE
        dialogRatingBinding.btnRate.visibility = View.GONE
        dialogRatingBinding.animationEmojis.visibility = View.INVISIBLE
        dialogRatingBinding.imgStar.visibility = View.VISIBLE
        mRateDialogShowing = false
        mLoadingAdsDialogShowing = false
        mLoadingProgressBarShowing = false
    }

    fun dismissRatingDialog() {
        if (mRateDialogShowing) {
            val view = activity.findViewById<View>(android.R.id.content) as ViewGroup
            view.removeViewAt(view.childCount - 1)
            mRateDialogShowing = false
        }
    }

    fun dismissLoadingAdDialog() {
        if (mLoadingAdsDialogShowing) {
            val view = activity.findViewById<View>(android.R.id.content) as ViewGroup
            view.removeViewAt(view.childCount - 1)
            mLoadingAdsDialogShowing = false
        }
    }

    fun dismissLoadingBarDialog() {
        if (mLoadingProgressBarShowing) {
            val view = activity.findViewById<View>(android.R.id.content) as ViewGroup
            view.removeViewAt(view.childCount - 1)
            mLoadingProgressBarShowing = false
        }
    }

    private var mAutoShowRating = false
    fun showRatingDialog(autoShow: Boolean = true, onRate: (Int) -> Unit) {
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
                    setUpMessageAndEmoij(
                        activity.resources.getString(R.string.oh_no),
                        activity.resources.getString(R.string.please_leave_us_some_feedback),
                        R.raw.animation_2star
                    )
                }

                3 -> {
                    setUpMessageAndEmoij(
                        activity.resources.getString(R.string.oh_no),
                        activity.resources.getString(R.string.please_leave_us_some_feedback),
                        R.raw.animation_3star
                    )
                }

                4 -> {
                    setUpMessageAndEmoij(
                        activity.resources.getString(R.string.we_like_you_too),
                        activity.resources.getString(R.string.thanks_for_your_feedback),
                        R.raw.animation_4star
                    )
                }

                else -> {
                    Glide.with(activity.applicationContext).load(R.drawable.ic_5stars)
                        .into(dialogRatingBinding.imgStar)
                    setUpMessageAndEmoij(
                        activity.resources.getString(R.string.we_like_you_too),
                        activity.resources.getString(R.string.thanks_for_your_feedback),
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
        dialogRatingBinding.btnClose.setOnClickListener {
            dismissRatingDialog()
        }
        dialogRatingBinding.bgBlackViewInRate.fadeInAnimation()
        dialogRatingBinding.mainRatingContentLayout.scaleAnimation()
        dialogRatingBinding.txtDontAskAgain.setOnClickListener {
            dismissRatingDialog()
            AppPreferences().isRated = true
        }
        dialogRatingBinding.btnRate.setOnClickListener {
            AppPreferences().isRated = true
//            if (rating <= 3) {
//                FeedbackActivity.start(this, rating)
//            } else {
//                openAppInStore()
//            }
            onRate(rating)
            dismissRatingDialog()
            if (autoShow)
                activity.finishAfterTransition()
        }
    }

    private fun setUpMessageAndEmoij(label: String, message: String, emoij: Int) {
        dialogRatingBinding.layoutRateDialogTitle.text = label
        dialogRatingBinding.txtDescription.text = message
        dialogRatingBinding.animationEmojis.setAnimation(emoij)
    }

    fun showLoadingAdsDialog() {
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
        val view = activity.findViewById<View>(android.R.id.content) as ViewGroup
        layoutLoadingBinding =
            LayoutLoadingBinding.inflate(activity.layoutInflater, view, true)
    }
}