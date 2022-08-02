package com.abc.mirroring.ui.browsermirror

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.net.Uri
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.abc.mirroring.R
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivityBrowserMirrorBinding
import com.abc.mirroring.databinding.LayoutDialogDisconnectBrowserMirrorBinding
import com.abc.mirroring.service.ServiceMessage
import com.abc.mirroring.service.helper.IntentAction
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
    }

    private val settingsListener = object : SettingsReadOnly.OnSettingsChangeListener {
        override fun onSettingsChanged(key: String) {
            if (key == Settings.Key.NIGHT_MODE) AppCompatDelegate.setDefaultNightMode(settings.nightMode)
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

    override fun onStop() {
        settings.unregisterChangeListener(settingsListener)
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
                    if (serviceMessage.isStreaming) {
                        visibility = View.VISIBLE
                        isEnabled = !serviceMessage.isBusy
                        setOnClickListener {
                            showDisconnectDialog()
                        }
                        text = getString(R.string.stop_stream)
                    } else {
                        setOnClickListener { startStreamScreen() }
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
        if (settings.severPort != newPort) settings.severPort = newPort
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
                        "http://${netInterface.address.asString()}:${settings.severPort}"
                    Timber.d("fullAddress $fullAddress")
                    binding.txtIpAddress.text = fullAddress.setUnderlineSpan()
                    binding.txtIpAddress.setOnClickListener {
                        openInBrowser(fullAddress)
                    }
                    binding.btnCopy.setOnClickListener {
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText(
                                binding.txtIpAddress.text,
                                binding.txtIpAddress.text
                            )
                        )
                        Toast.makeText(this, R.string.stream_fragment_copied, Toast.LENGTH_LONG)
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
}