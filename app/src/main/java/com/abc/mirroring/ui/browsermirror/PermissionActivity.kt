package com.abc.mirroring.ui.browsermirror

import android.app.Activity
import android.content.*
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.coroutineScope
import androidx.viewbinding.ViewBinding
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.service.AppService
import com.abc.mirroring.service.ServiceMessage
import com.abc.mirroring.service.helper.IntentAction
import com.afollestad.materialdialogs.MaterialDialog
import com.elvishew.xlog.XLog
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.dvkr.screenstream.data.other.getLog
import info.dvkr.screenstream.data.settings.Settings
import info.dvkr.screenstream.data.settings.SettingsImpl
import info.dvkr.screenstream.data.settings.SettingsReadOnly
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber


abstract class PermissionActivity<V : ViewBinding> : BaseActivity<V>() {

    companion object {
        private const val CAST_PERMISSION_PENDING_KEY = "CAST_PERMISSION_PENDING_KEY"
        private const val SCREEN_CAPTURE_REQUEST_CODE = 10
        var settings: Settings? = null
    }

    private var permissionsErrorDialog: MaterialDialog? = null
    private var isCastPermissionsPending: Boolean = false

    private var serviceMessageFlowJob: Job? = null
    private var isBound: Boolean = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            Timber.d("onServiceConnected")
            serviceMessageFlowJob =
                lifecycle.coroutineScope.launch(CoroutineName("ServiceActivity.ServiceMessageFlow")) {
                    (service as AppService.AppServiceBinder).getServiceMessageFlow()
                        .onEach { serviceMessage ->
                            XLog.v(this.getLog("onServiceMessage", "$serviceMessage"))
                            StreamViewModel.getInstance().serviceMessageLiveData.value =
                                serviceMessage
                        }
                        .catch { cause -> XLog.e(this.getLog("onServiceMessage"), cause) }
                        .collect()
                }

            isBound = true
            IntentAction.GetServiceState.sendToAppService(this@PermissionActivity)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            XLog.w(this.getLog("onServiceDisconnected"))
            serviceMessageFlowJob?.cancel()
            serviceMessageFlowJob = null
            isBound = false
        }
    }

    private val settingsListener = object : SettingsReadOnly.OnSettingsChangeListener {
        override fun onSettingsChanged(key: String) {
            if (key == Settings.Key.NIGHT_MODE) AppCompatDelegate.setDefaultNightMode(settings!!.nightMode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = initBinding()
        setContentView(binding.root)
        settings = SettingsImpl(
            BinaryPreferencesBuilder(applicationContext)
                .supportInterProcess(true)
                .exceptionHandler { ex -> Timber.e(ex) }
                .build()
        )
        AppCompatDelegate.setDefaultNightMode(settings!!.nightMode)
        isCastPermissionsPending =
            savedInstanceState?.getBoolean(CAST_PERMISSION_PENDING_KEY) ?: false
        XLog.d(getLog("onCreate", "isCastPermissionsPending: $isCastPermissionsPending"))
        super.onCreate(savedInstanceState)
        initViews()
        initActions()
    }

    override fun onStart() {
        super.onStart()
        StreamViewModel.getInstance().serviceMessageLiveData.observe(this) { message ->
            message?.let {
                onServiceMessage(
                    it
                )
            }
        }
        bindService(
            AppService.getAppServiceIntent(this),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        settings!!.registerChangeListener(settingsListener)
    }

    override fun onResume() {
        super.onResume()
        IntentAction.GetServiceState.sendToAppService(this)
    }

    override fun onStop() {
        if (isBound) {
            serviceMessageFlowJob?.cancel()
            serviceMessageFlowJob = null
            unbindService(serviceConnection)
            isBound = false
        }

        settings!!.unregisterChangeListener(settingsListener)
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        XLog.d(getLog("onSaveInstanceState", "isCastPermissionsPending: $isCastPermissionsPending"))
        outState.putBoolean(CAST_PERMISSION_PENDING_KEY, isCastPermissionsPending)
        super.onSaveInstanceState(outState)
    }

    @CallSuper
    open fun onServiceMessage(serviceMessage: ServiceMessage) {
        when (serviceMessage) {
            is ServiceMessage.ServiceState -> {
                if (serviceMessage.isWaitingForPermission) {
                    if (isCastPermissionsPending) {
                        XLog.i(
                            getLog(
                                "onServiceMessage",
                                "Ignoring: isCastPermissionsPending == true"
                            )
                        )
                    } else {
                        requestProjectionPermission()
                    }
                } else {
                    isCastPermissionsPending = false
                }
            }
            else -> {}
        }
    }

    protected fun requestProjectionPermission() {
        isCastPermissionsPending = true
        permissionsErrorDialog?.dismiss()
        permissionsErrorDialog = null
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        try {
//                            val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
//                            val options = ActivityOptions.makeBasic()
//                            options.launchDisplayId = dm.displays[1].displayId
            val createScreenCaptureIntent =
                projectionManager.createScreenCaptureIntent()
            startActivityForResult(
                createScreenCaptureIntent,
                SCREEN_CAPTURE_REQUEST_CODE//,options.toBundle()
            )
        } catch (ex: ActivityNotFoundException) {
//                            showErrorDialog(
//                                R.string.permission_activity_error_title_activity_not_found,
//                                R.string.permission_activity_error_activity_not_found
//                            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                XLog.d(getLog("onActivityResult", "Cast permission granted"))
                require(data != null) { "onActivityResult: data = null" }
                IntentAction.CastIntent(data).sendToAppService(this@PermissionActivity)
            } else {
                XLog.w(getLog("onActivityResult", "Cast permission denied"))
                IntentAction.CastPermissionsDenied.sendToAppService(this@PermissionActivity)
                isCastPermissionsPending = false
                permissionDenied()


//                showErrorDialog(
//                    R.string.permission_activity_cast_permission_required_title,
//                    R.string.permission_activity_cast_permission_required
//                )
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    abstract override fun initBinding(): V
    abstract override fun initViews()
    abstract override fun initActions()
    protected open fun permissionDenied() {}
}