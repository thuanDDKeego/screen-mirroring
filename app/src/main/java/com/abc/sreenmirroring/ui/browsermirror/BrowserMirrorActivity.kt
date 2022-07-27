package com.abc.sreenmirroring.ui.browsermirror

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.net.Uri
import android.net.wifi.WifiManager
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.config.AppPreferences
import com.abc.sreenmirroring.databinding.ActivityBrowserMirrorBinding
import com.abc.sreenmirroring.databinding.LayoutDialogDisconnectBrowserMirrorBinding
import com.abc.sreenmirroring.service.ServiceMessage
import com.abc.sreenmirroring.service.helper.IntentAction
import com.elvishew.xlog.XLog
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.dvkr.screenstream.data.model.AppError
import info.dvkr.screenstream.data.model.FixableError
import info.dvkr.screenstream.data.other.asString
import info.dvkr.screenstream.data.other.getLog
import info.dvkr.screenstream.data.other.setUnderlineSpan
import info.dvkr.screenstream.data.settings.Settings
import info.dvkr.screenstream.data.settings.SettingsImpl
import info.dvkr.screenstream.data.settings.SettingsReadOnly
import timber.log.Timber


class BrowserMirrorActivity : PermissionActivity<ActivityBrowserMirrorBinding>() {

    private var isStopStream: Boolean = false
    private var lastServiceMessage: ServiceMessage.ServiceState? = null
    private val clipboard: ClipboardManager? by lazy {
        ContextCompat.getSystemService(this, ClipboardManager::class.java)
    }

    var viewModel: StreamViewModel? = null

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, BrowserMirrorActivity::class.java)
            activity.startActivity(intent)
        }

        fun getAppActivityIntent(context: Context): Intent =
            Intent(context.applicationContext, BrowserMirrorActivity::class.java)

        fun getStartIntent(context: Context): Intent =
            getAppActivityIntent(context)

        private const val SCREEN_CAPTURE_REQUEST_CODE = 10
    }

    override fun initBinding() = ActivityBrowserMirrorBinding.inflate(layoutInflater)

    override fun initViews() {
        binding.btnStopStream.visibility = View.GONE
        if (AppPreferences().isTurnOnPinCode == true) {
            binding.txtPinCode.text = "Pin: ${AppPreferences().pinCode}"
        } else {
            binding.txtPinCode.visibility = View.GONE
            binding.txtSecurity.visibility = View.GONE
        }
        binding.txtWifiName.text = getWifiName()
        settings = SettingsImpl(
            BinaryPreferencesBuilder(applicationContext)
                .supportInterProcess(true)
                .exceptionHandler { ex -> Timber.e(ex) }
                .build()
        )
        viewModel = ViewModelProvider(this)[StreamViewModel::class.java]
        Timber.d("start browser $intent")
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
        this.getServiceMessageLiveData().observe(this) { serviceMessage ->
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
                    visibility = View.VISIBLE
                    isEnabled = !serviceMessage.isBusy
                    if (serviceMessage.isStreaming) {
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

    private fun getWifiName(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        return info.ssid.replace("\"", "")
    }

    private fun showDialogStopService() {
        AlertDialog.Builder(this)
            .setTitle("Want to disconnect?")
            .setMessage("Connection will be interrupted.")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                Timber.d("onPress Stop Stream")
                stopStreamScreen()
            }
            .setNegativeButton(android.R.string.no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
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
        Timber.d("startStreamScreen")
        IntentAction.StartStream.sendToAppService(this@BrowserMirrorActivity)
        isStopStream = false
    }

    private fun stopStreamScreen() {
        Timber.d("stopStreamScreen")
        IntentAction.StopStream.sendToAppService(this@BrowserMirrorActivity)
        isStopStream = true
        NotificationManagerCompat.from(this).cancelAll()
        finish()
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