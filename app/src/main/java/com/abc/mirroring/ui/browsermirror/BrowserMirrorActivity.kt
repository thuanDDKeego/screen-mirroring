package com.abc.mirroring.ui.browsermirror

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.ClipboardManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.abc.mirroring.R
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivityBrowserMirrorBinding
import com.abc.mirroring.databinding.LayoutDialogDisconnectBrowserMirrorBinding
import com.abc.mirroring.service.ServiceMessage
import com.abc.mirroring.service.helper.IntentAction
import com.abc.mirroring.ui.dialog.DialogCenter
import com.abc.mirroring.ui.home.HomeActivity
import com.abc.mirroring.ui.settings.SettingActivity
import com.abc.mirroring.ui.tutorial.TutorialActivity
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import com.elvishew.xlog.XLog
import info.dvkr.screenstream.data.model.AppError
import info.dvkr.screenstream.data.model.FixableError
import info.dvkr.screenstream.data.other.asString
import info.dvkr.screenstream.data.other.getLog
import info.dvkr.screenstream.data.other.setUnderlineSpan
import info.dvkr.screenstream.data.settings.Settings
import info.dvkr.screenstream.data.settings.SettingsReadOnly
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


class BrowserMirrorActivity : PermissionActivity<ActivityBrowserMirrorBinding>() {

    private var isStopStream: Boolean = false
    private var lastServiceMessage: ServiceMessage.ServiceState? = null
    private val clipboard: ClipboardManager? by lazy {
        ContextCompat.getSystemService(this, ClipboardManager::class.java)
    }
    private lateinit var dialogCenter: DialogCenter

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, BrowserMirrorActivity::class.java)
            activity.startActivity(intent)
        }

        fun getAppActivityIntent(context: Context): Intent =
            Intent(context.applicationContext, BrowserMirrorActivity::class.java)

        fun getStartIntent(context: Context): Intent =
            getAppActivityIntent(context)

        const val START_WHEN_RUNNING_REQUEST_CODE = 10
    }

    override fun initBinding() = ActivityBrowserMirrorBinding.inflate(layoutInflater)

    override fun initViews() {
        dialogCenter = DialogCenter(this)
        FirebaseTracking.logBrowserStartShowed()
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            startStreamScreen()
        }
        binding.btnStopStream.visibility = View.GONE
        if (AppPreferences().isTurnOnPinCode == true) {
            binding.txtPinCode.text = "Pin: ${AppPreferences().pinCode}"
            binding.txtSecurity.visibility = View.VISIBLE
            binding.txtSecurity.text =
                Html.fromHtml(getString(R.string.security_your_screen_mirroring))
            binding.txtSecurity.makeLinks(
                Pair(getString(R.string.setting), View.OnClickListener {
                    SettingActivity.gotoActivity(this@BrowserMirrorActivity)
                })
            )
        } else {
            binding.txtPinCode.visibility = View.GONE
            binding.txtSecurity.visibility = View.GONE
        }
        observerWifiState(object : onWifiChangeStateConnection {
            override fun onWifiUnavailable() {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.txtWifiName.text = getString(R.string.wifi_not_connected)
                    binding.txtWifiName.setTextColor(
                        ContextCompat.getColor(
                            this@BrowserMirrorActivity, R.color.draw_red
                        )
                    )
                    HomeActivity.isStreamingBrowser.value = false
                }
            }

            override fun onWifiAvailable() {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.txtWifiName.text = getString(R.string.wifi_connected)
                    binding.txtWifiName.setTextColor(
                        ContextCompat.getColor(
                            this@BrowserMirrorActivity, R.color.black
                        )
                    )
                }
            }
        })
    }

    override fun initActions() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.imgHelp.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.BrowserMirror_Click_Tutorial)
            TutorialActivity.gotoActivity(this@BrowserMirrorActivity)
        }
        binding.imgBattery.setOnClickListener {
            dialogCenter.showDialog(DialogCenter.DialogType.StopOptimizeBattery)
        }
    }

    override fun permissionDenied() {
        binding.btnStopStream.apply {
            visibility = View.VISIBLE
            text = getString(R.string.start_stream)
            setOnClickListener {
                requestProjectionPermission()
                visibility = View.GONE
            }
        }
    }

    private val settingsListener = object : SettingsReadOnly.OnSettingsChangeListener {
        override fun onSettingsChanged(key: String) {
            if (key == Settings.Key.NIGHT_MODE) AppCompatDelegate.setDefaultNightMode(settings!!.nightMode)
        }
    }

    private fun routeIntentAction(intent: Intent?) {
        val intentAction = IntentAction.fromIntent(intent)
        intentAction != null || return
        Timber.d("routeIntentAction $intentAction")
        when (intentAction) {
            IntentAction.StartStream -> startStreamScreen()
            else -> {}
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        routeIntentAction(intent)
    }

    override fun onStart() {
        super.onStart()
        StreamViewModel.getInstance().serviceMessageLiveData.observe(this) { serviceMessage ->
            Timber.d("onStart $serviceMessage")
            when (serviceMessage) {
                is ServiceMessage.ServiceState -> onServiceStateMessage(serviceMessage)
                else -> {}
            }
        }

        IntentAction.GetServiceState.sendToAppService(this@BrowserMirrorActivity)
    }

    override fun onResume() {
        super.onResume()
        if (AppPreferences().isTurnOnPinCode == true) {
            binding.txtPinCode.text = "Pin: ${AppPreferences().pinCode}"
            binding.txtSecurity.visibility = View.VISIBLE
            binding.txtSecurity.text =
                Html.fromHtml(getString(R.string.security_your_screen_mirroring))
            binding.txtSecurity.makeLinks(
                Pair(getString(R.string.setting), View.OnClickListener {
                    SettingActivity.gotoActivity(this@BrowserMirrorActivity)
                })
            )
        } else {
            binding.txtPinCode.visibility = View.GONE
            binding.txtSecurity.visibility = View.GONE
        }
    }

    override fun onStop() {
        settings!!.unregisterChangeListener(settingsListener)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationManagerCompat.from(this).cancelAll()
    }

    @SuppressLint("RestrictedApi")
    override fun onServiceMessage(serviceMessage: ServiceMessage) {
        super.onServiceMessage(serviceMessage)

        when (serviceMessage) {
            is ServiceMessage.ServiceState -> {
                Timber.d("onServiceMessage lastServiceMessage $lastServiceMessage $serviceMessage")
                lastServiceMessage != serviceMessage || return
                with(binding.btnStopStream) {
                    FirebaseTracking.log(FirebaseLogEvent.Browser_Mirror_Click_Stop_Stream)
                    if (serviceMessage.isStreaming) {
                        visibility = View.VISIBLE
                        isEnabled = !serviceMessage.isBusy
                        setOnClickListener {
                            showDisconnectDialog()
                            AppPreferences().countSatisfied = AppPreferences().countSatisfied!! + 1
                        }
                        text = getString(R.string.stop_stream)
                    } else {
                        setOnClickListener {
                            FirebaseTracking.log(FirebaseLogEvent.Browser_Mirror_Click_Start_Stream)
                            val connManager =
                                getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                            val networkInfo =
                                connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                            if (networkInfo?.detailedState == NetworkInfo.DetailedState.DISCONNECTED) {
                                Toast.makeText(
                                    this@BrowserMirrorActivity,
                                    getString(R.string.checking_your_wifi),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                startStreamScreen()
                            }
                        }
                        text = getString(R.string.start_stream)
                    }
                }
                if (serviceMessage.isStreaming != lastServiceMessage?.isStreaming) {

                }

                lastServiceMessage = serviceMessage
            }

            else -> {}
        }
    }

    private fun setNewPortAndReStart() {
        val newPort = (1025..65535).random()
        Timber.d("setNewPortAndReStart $newPort")
        if (settings!!.severPort != newPort) settings!!.severPort = newPort
        IntentAction.StartStream.sendToAppService(this@BrowserMirrorActivity)
    }

    private fun showError(appError: AppError?) {
        Timber.d("showError $appError")
        when (appError) {
            is FixableError.AddressInUseException -> setNewPortAndReStart()
            else -> {}
        }
    }

    private fun startStreamScreen() {
        IntentAction.StartStream.sendToAppService(this@BrowserMirrorActivity)
        isStopStream = false
    }

    private fun stopStreamScreen() {
        IntentAction.StopStream.sendToAppService(this@BrowserMirrorActivity)
        isStopStream = true
//        NotificationManagerCompat.from(this).cancelAll()
//        finish()
    }

    private fun onServiceStateMessage(serviceMessage: ServiceMessage.ServiceState) {
        // Interfaces
        if (serviceMessage.netInterfaces.isEmpty()) {

        } else {
            serviceMessage.netInterfaces.sortedBy { it.address.asString() }
                .forEach { netInterface ->
                    val fullAddress =
                        "http://${netInterface.address.asString()}:${settings!!.severPort}"
                    Timber.d("fullAddress $fullAddress")
                    binding.txtIpAddress.text = fullAddress.setUnderlineSpan()
                    binding.txtIpAddress.setOnClickListener {
                        openInBrowser(fullAddress)
                    }
                    binding.btnCopy.setOnClickListener {
                        FirebaseTracking.log(FirebaseLogEvent.Browser_Mirror_Click_Copy)
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText(
                                binding.txtIpAddress.text,
                                binding.txtIpAddress.text
                            )
                        )
                        Toast.makeText(this, R.string.stream_fragment_copied, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }

        // Hide pin on Start
//        if (settingsReadOnly.enablePin) {
//
//        } else {
//
//        }

        showError(serviceMessage.appError)
    }

    private fun openInBrowser(fullAddress: String) {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(fullAddress)
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (ex: ActivityNotFoundException) {
            XLog.w(getLog("openInBrowser", ex.toString()))
        } catch (ex: SecurityException) {
            XLog.w(getLog("openInBrowser", ex.toString()))
        }
    }

    //disconnect dialog
    private lateinit var dialogDisconnectBinding: LayoutDialogDisconnectBrowserMirrorBinding
    private var mDialogDisconnectIsShowing: Boolean = false
    private fun showDisconnectDialog() {
        if (mDialogDisconnectIsShowing) return
        mDialogDisconnectIsShowing = true
        val view = findViewById<View>(android.R.id.content) as ViewGroup
        dialogDisconnectBinding =
            LayoutDialogDisconnectBrowserMirrorBinding.inflate(layoutInflater, view, true)
        dialogDisconnectBinding.apply {
            constraintBgDialogDisconnect.setOnClickListener {
                dismissDisconnectDialog()
            }
            cardDialog.setOnClickListener {}
            txtOk.setOnClickListener {
                stopStreamScreen()
                dismissDisconnectDialog()
            }
            txtCancel.setOnClickListener {
                dismissDisconnectDialog()
            }
        }
    }

    private fun dismissDisconnectDialog() {
        if (mDialogDisconnectIsShowing) {
            val view = findViewById<View>(android.R.id.content) as ViewGroup
            view.removeViewAt(view.childCount - 1)
            mDialogDisconnectIsShowing = false
        }
    }

    override fun finish() {
        val returnIntent = Intent()
        returnIntent.putExtra(HomeActivity.SHOW_RATING_DIALOG, true)
        setResult(RESULT_OK, returnIntent)
        super.finish()
    }
}

fun AppCompatTextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                // use this to change the link color
                textPaint.color = textPaint.linkColor
                // toggle below value to enable/disable
                // the underline shown below the clickable text
                textPaint.isUnderlineText = false
            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
        if (startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}