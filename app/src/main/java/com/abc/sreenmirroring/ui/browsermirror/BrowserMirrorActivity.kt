package com.abc.sreenmirroring.ui.browsermirror

import android.app.Activity
import android.content.*
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityBrowserMirrorBinding
import com.abc.sreenmirroring.service.AppService
import com.abc.sreenmirroring.service.ServiceMessage
import com.abc.sreenmirroring.service.helper.IntentAction
import com.elvishew.xlog.XLog
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.dvkr.screenstream.data.other.asString
import info.dvkr.screenstream.data.other.getLog
import info.dvkr.screenstream.data.other.setUnderlineSpan
import info.dvkr.screenstream.data.settings.Settings
import info.dvkr.screenstream.data.settings.SettingsImpl
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class BrowserMirrorActivity : BaseActivity<ActivityBrowserMirrorBinding>() {
    private lateinit var settings: Settings

    private val serviceMessageLiveData = MutableLiveData<ServiceMessage>()
    private var isCastPermissionGranted: Boolean = false
    private var isCastPermissionsPending: Boolean = false
    private var isCheckedPermission: Boolean = false
    private var isStopStream: Boolean = false

    private var serviceMessageFlowJob: Job? = null
    private var isBound: Boolean = false

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, BrowserMirrorActivity::class.java)
            activity.startActivity(intent)
        }

        private const val SCREEN_CAPTURE_REQUEST_CODE = 10
    }

    override fun initBinding() = ActivityBrowserMirrorBinding.inflate(layoutInflater)

    override fun initViews() {
        settings = SettingsImpl(
            BinaryPreferencesBuilder(applicationContext)
                .supportInterProcess(true)
                .exceptionHandler { ex -> Timber.e(ex) }
                .build()
        )
    }

    override fun initActions() {
        binding.btnStopStream.setOnClickListener {
            IntentAction.StartStream.sendToAppService(this@BrowserMirrorActivity)
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")

        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        IntentAction.CastIntent(projectionManager.createScreenCaptureIntent())
            .sendToAppService(this@BrowserMirrorActivity)

        serviceMessageLiveData.observe(this) { serviceMessage ->
            when (serviceMessage) {
                is ServiceMessage.ServiceState -> onServiceStateMessage(serviceMessage)
                else -> {}
            }
        }
        bindService(
            AppService.getAppServiceIntent(this),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Timber.d("Cast permission granted")
                require(data != null) { }
                IntentAction.CastIntent(data).sendToAppService(this@BrowserMirrorActivity)
                isCastPermissionGranted = true
            } else {
                Timber.d("Cast permission denied")
                IntentAction.CastPermissionsDenied.sendToAppService(this@BrowserMirrorActivity)
                isCastPermissionGranted = false
                isCastPermissionsPending = false

                showErrorDialog()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.d("onServiceConnected")
            serviceMessageFlowJob =
                lifecycle.coroutineScope.launch(CoroutineName("StreamActivity.ServiceMessageFlow")) {
                    (service as AppService.AppServiceBinder).getServiceMessageFlow()
                        .onEach { serviceMessage ->
                            Timber.d("onServiceMessage onEach: $serviceMessage")
                            serviceMessageLiveData.value = serviceMessage
                        }
                        .catch { cause -> Timber.d("onServiceMessage catch: $cause") }
                }

            isBound = true
            IntentAction.GetServiceState.sendToAppService(this@BrowserMirrorActivity)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.d("onServiceDisconnected $name")
        }
    }

    private fun startStreamScreen() {
        IntentAction.StartStream.sendToAppService(this@BrowserMirrorActivity)
    }

    private fun onServiceStateMessage(serviceMessage: ServiceMessage.ServiceState) {
        Timber.d("onServiceStateMessage ${serviceMessage.isStreaming} -- ${serviceMessage.isBusy} -- ${serviceMessage.isWaitingForPermission}")
        checkPermission(serviceMessage)
        if (!isStopStream && serviceMessage.appError == null && !serviceMessage.isStreaming && !serviceMessage.isWaitingForPermission && !serviceMessage.isBusy) {
            startStreamScreen()
        }

        if (serviceMessage.isStreaming) {
            binding.btnStopStream.visibility = View.VISIBLE
            binding.btnStopStream.isEnabled = true
        }
        if (serviceMessage.netInterfaces.isEmpty()) {
//            with(
//                ItemDeviceAddressBinding.inflate(
//                    layoutInflater,
//                    binding.llFragmentStreamAddresses,
//                    false
//                )
//            ) {
//                tvItemDeviceAddressName.text = ""
//                binding.llFragmentStreamAddresses.addView(this.root)
//            }
        } else {
            serviceMessage.netInterfaces.sortedBy { it.address.asString() }
                .forEach { netInterface ->
                    val fullAddress =
                        "http://${netInterface.address.asString()}:${settings.severPort}"
                    binding.txtIpAddress.text = fullAddress.setUnderlineSpan()

                }
        }
    }

    private fun checkPermission(serviceMessage: ServiceMessage.ServiceState) {
        Timber.d("checkPermission $isCheckedPermission")
        if (serviceMessage.isWaitingForPermission && !isCheckedPermission) {
            isCastPermissionsPending = true

            val projectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            try {
                val createScreenCaptureIntent = projectionManager.createScreenCaptureIntent()
                startActivityForResult(
                    createScreenCaptureIntent, SCREEN_CAPTURE_REQUEST_CODE//,options.toBundle()
                )
                isCheckedPermission = true
            } catch (ex: ActivityNotFoundException) {
                isCheckedPermission = true
                showErrorDialog()
            }
        } else {
            isCastPermissionsPending = false
        }
    }

    private fun showErrorDialog() {
        Timber.d("showErrorDialog")
    }
}