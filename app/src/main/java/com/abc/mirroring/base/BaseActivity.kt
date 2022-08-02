package com.abc.mirroring.base

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.*
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.abc.mirroring.R
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.LayoutDialogLoadingAdsBinding
import com.abc.mirroring.databinding.LayoutRateDialogBinding
import com.abc.mirroring.extentions.fadeInAnimation
import com.abc.mirroring.extentions.scaleAnimation
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


abstract class BaseActivity<V : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: V
    var mRateDialogShowing = false
    var mLoadingAdDialogShowing = false
    open var isFullScreen: Boolean = false
    private lateinit var dialogRatingBinding: LayoutRateDialogBinding
    private lateinit var dialogLoadingAdBinding: LayoutDialogLoadingAdsBinding

    companion object {
        var dLocale: Locale? = Locale(AppPreferences().languageSelected.toString())
    }

    init {
        if (dLocale != Locale("")) {
            Locale.setDefault(dLocale)
            val configuration = Configuration()
            configuration.setLocale(dLocale)
            this.applyOverrideConfiguration(configuration)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = initBinding()
        if (isFullScreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setContentView(binding.root)
        initViews()
        initActions()
        initAdmob()
    }

    private fun resetDialogView() {
        dialogRatingBinding.txtDescription.visibility = View.GONE
        dialogRatingBinding.btnRate.visibility = View.GONE
        dialogRatingBinding.btnClose.visibility = View.GONE
        dialogRatingBinding.animationEmojis.visibility = View.INVISIBLE
        dialogRatingBinding.imgStar.visibility = View.VISIBLE
    }

    private fun dismissRatingDialog() {
        if (mRateDialogShowing) {
            val view = findViewById<View>(android.R.id.content) as ViewGroup
            view.removeViewAt(view.childCount - 1)
            mRateDialogShowing = false
        }
    }

    protected fun dismissLoadingAdDialog() {
        if (mLoadingAdDialogShowing) {
            val view = findViewById<View>(android.R.id.content) as ViewGroup
            view.removeViewAt(view.childCount - 1)
            mLoadingAdDialogShowing = false
        }
    }

    private fun Activity.openAppInStore() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.android.vending")
            if (intent != null) {
                intent.action = Intent.ACTION_VIEW
                intent.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
        }
    }

    protected fun observerWifiState(onWifiChangeStateConnection: onWifiChangeStateConnection) {
        val connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (networkInfo?.detailedState == NetworkInfo.DetailedState.DISCONNECTED) {
            onWifiChangeStateConnection.onWifiUnavailable()
        }
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                onWifiChangeStateConnection.onWifiAvailable()
            }

            // lost network connection
            override fun onLost(network: Network) {
                super.onLost(network)
                onWifiChangeStateConnection.onWifiUnavailable()
            }
        }
        val connectivityManager =
            getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private var mAutoShowRating = false
    protected fun showRatingDialog(autoShow: Boolean = true) {
        if (mRateDialogShowing) return
        mRateDialogShowing = true
        val view = findViewById<View>(android.R.id.content) as ViewGroup
        dialogRatingBinding =
            LayoutRateDialogBinding.inflate(layoutInflater, view, true)
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
        dialogRatingBinding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            resetDialogView()
            dialogRatingBinding.btnClose.visibility = View.GONE
            dialogRatingBinding.btnRate.visibility = View.VISIBLE
            dialogRatingBinding.txtDescription.visibility = View.VISIBLE
            dialogRatingBinding.animationEmojis.visibility = View.VISIBLE
            dialogRatingBinding.imgStar.visibility = View.INVISIBLE
            when (rating.toInt()) {
                0, 1, 2 -> {
                    dialogRatingBinding.layoutRateDialogTitle.text =
                        this@BaseActivity.resources.getString(R.string.oh_no)
                    dialogRatingBinding.txtDescription.text =
                        this@BaseActivity.resources.getString(R.string.please_leave_us_some_feedback)
                    dialogRatingBinding.animationEmojis.setAnimation(R.raw.animation_2star)
                }
                3 -> {
                    dialogRatingBinding.layoutRateDialogTitle.text =
                        this@BaseActivity.resources.getString(R.string.oh_no)
                    dialogRatingBinding.txtDescription.text =
                        this@BaseActivity.resources.getString(R.string.please_leave_us_some_feedback)
                    dialogRatingBinding.animationEmojis.setAnimation(R.raw.animation_3star)
                }
                4 -> {
                    dialogRatingBinding.layoutRateDialogTitle.text =
                        this@BaseActivity.resources.getString(R.string.we_like_you_too)
                    dialogRatingBinding.txtDescription.text =
                        this@BaseActivity.resources.getString(R.string.thanks_for_your_feedback)
                    dialogRatingBinding.animationEmojis.setAnimation(R.raw.animation_4star)
                }
                else -> {
                    Glide.with(view.context).load(R.drawable.ic_5stars)
                        .into(dialogRatingBinding.imgStar)
                    dialogRatingBinding.layoutRateDialogTitle.text =
                        this@BaseActivity.resources.getString(R.string.we_like_you_too)
                    dialogRatingBinding.txtDescription.text =
                        this@BaseActivity.resources.getString(R.string.thanks_for_your_feedback)
                    dialogRatingBinding.animationEmojis.setAnimation(R.raw.animation_5star)
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
        dialogRatingBinding.txtRemindALater.setOnClickListener { dismissRatingDialog() }
        dialogRatingBinding.btnRate.setOnClickListener {
            openAppInStore()
            dismissRatingDialog()
            if (autoShow)
                finishAfterTransition()
        }
    }

    protected fun showLoadingAdDialog() {
        if (mLoadingAdDialogShowing) return
        mLoadingAdDialogShowing = true
        val view = findViewById<View>(android.R.id.content) as ViewGroup
        dialogLoadingAdBinding =
            LayoutDialogLoadingAdsBinding.inflate(layoutInflater, view, true)
        val animMoveRightLoadBar = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.move_right_load_bar
        )
        dialogLoadingAdBinding.viewLoadBar.startAnimation(animMoveRightLoadBar)
        dialogLoadingAdBinding.constraintBgDialogLoadingAd.setOnClickListener {}
    }

    abstract fun initBinding(): V
    abstract fun initViews()
    abstract fun initActions()
    open fun initAdmob() {}
    interface onWifiChangeStateConnection {
        fun onWifiUnavailable()
        fun onWifiAvailable()
    }
}