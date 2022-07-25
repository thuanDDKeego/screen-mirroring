package com.abc.sreenmirroring.ui.browsermirror

import android.app.Activity
import android.content.*
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.config.AppPreferences
import com.abc.sreenmirroring.databinding.ActivityBrowserMirrorBinding
import com.abc.sreenmirroring.service.AppService
import com.abc.sreenmirroring.service.ServiceMessage
import com.abc.sreenmirroring.service.helper.IntentAction
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.dvkr.screenstream.data.model.AppError
import info.dvkr.screenstream.data.model.FixableError
import info.dvkr.screenstream.data.other.asString
import info.dvkr.screenstream.data.other.setUnderlineSpan
import info.dvkr.screenstream.data.settings.Settings
import info.dvkr.screenstream.data.settings.SettingsImpl
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class BrowserMirrorActivity : BaseActivity<ActivityBrowserMirrorBinding>() {

    private lateinit var settings: Settings

    private var isCastPermissionGranted: Boolean = false
    private var isCheckedPermission: Boolean = false
    private var isCastPermissionsPending: Boolean = false
    private var isStopStream: Boolean = false
    private var isBound: Boolean = false

    private val serviceMessageLiveData = MutableLiveData<ServiceMessage>()
    private var serviceMessageFlowJob: Job? = null

    var viewModel: StreamViewModel? = null

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, BrowserMirrorActivity::class.java)
            activity.startActivity(intent)
        }

        private const val SCREEN_CAPTURE_REQUEST_CODE = 10
    }

    override fun initBinding() = ActivityBrowserMirrorBinding.inflate(layoutInflater)

    override fun initViews() {
        binding.btnStopStream.visibility = View.GONE
        if (AppPreferences().isTurnOnPinCode == true) {
            binding.txtPinCode.text = AppPreferences().pinCode
        } else {
            binding.txtPinCode.visibility = View.GONE
            binding.txtSecurity.visibility = View.GONE
        }
        settings = SettingsImpl(
            BinaryPreferencesBuilder(applicationContext)
                .supportInterProcess(true)
                .exceptionHandler { ex -> Timber.e(ex) }
                .build()
        )
        viewModel = ViewModelProvider(this)[StreamViewModel::class.java]
    }

    override fun initActions() {
        binding.btnStopStream.setOnClickListener {
            if (isStopStream) {
                startStreamScreen()
            } else {
                showDialogStopService()
            }
        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        IntentAction.CastIntent(projectionManager.createScreenCaptureIntent())
            .sendToAppService(this@BrowserMirrorActivity)

        serviceMessageLiveData.observe(this) { serviceMessage ->
            Timber.d("onServiceStateMessage ${serviceMessage}")
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

    override fun onStop() {
        if (isBound) {
            serviceMessageFlowJob?.cancel()
            serviceMessageFlowJob = null
            unbindService(serviceConnection)
            isBound = false
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationManagerCompat.from(this).cancelAll()
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

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            serviceMessageFlowJob =
                lifecycle.coroutineScope.launch(CoroutineName("StreamActivity.ServiceMessageFlow")) {
                    (service as AppService.AppServiceBinder).getServiceMessageFlow()
                        .onEach { serviceMessage ->
                            Timber.d("onServiceMessage $serviceMessage")
                            serviceMessageLiveData.value = serviceMessage
                        }
                        .catch { cause -> Timber.d("onServiceMessage : $cause") }
                        .collect()
                }

            isBound = true
            IntentAction.GetServiceState.sendToAppService(this@BrowserMirrorActivity)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessageFlowJob?.cancel()
            serviceMessageFlowJob = null
            isBound = false
        }
    }

//    private fun showErrorDialog(
//        @StringRes titleRes: Int = R.string.permission_activity_error_title,
//        @StringRes messageRes: Int = R.string.permission_activity_error_unknown
//    ) {
//        permissionsErrorDialog?.dismiss()
//
//        permissionsErrorDialog = MaterialDialog(this).show {
//            lifecycleOwner(this@StreamActivity)
//            title(titleRes)
//            message(messageRes)
//            positiveButton(android.R.string.ok)
//            cancelable(false)
//            cancelOnTouchOutside(false)
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Timber.d("Cast permission granted")
                require(data != null) { "onActivityResult: data = null" }
                IntentAction.CastIntent(data).sendToAppService(this@BrowserMirrorActivity)
                isCastPermissionGranted = true
            } else {
                Timber.d("Cast permission denied")
                IntentAction.CastPermissionsDenied.sendToAppService(this@BrowserMirrorActivity)
                isCastPermissionGranted = false
                isCastPermissionsPending = false
//                showErrorDialog(
//                    R.string.permission_activity_cast_permission_required_title,
//                    R.string.permission_activity_cast_permission_required
//                )
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
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
        binding.btnStopStream.text = getString(R.string.stop_stream)
    }

    private fun stopStreamScreen() {
        IntentAction.StopStream.sendToAppService(this@BrowserMirrorActivity)
        isStopStream = true
        binding.btnStopStream.text = "Start Stream"
//        NotificationManagerCompat.from(this).cancelAll();
//        finish()
    }

    private fun onServiceStateMessage(serviceMessage: ServiceMessage.ServiceState) {
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
//                    showErrorDialog(
//                        R.string.permission_activity_error_title_activity_not_found,
//                        R.string.permission_activity_error_activity_not_found
//                    )
            }
        } else {
            isCastPermissionsPending = false
        }
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

                    Timber.d("fullAddress $fullAddress")
                    binding.txtIpAddress.text = fullAddress.setUnderlineSpan()

                }
        }

        showError(serviceMessage.appError)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isStopStream) {
            IntentAction.Exit.sendToAppService(this@BrowserMirrorActivity)
        }
    }

    private fun showNotification() {
//        val intent = Intent(this, App::class.java)
//
//        val pendingIntent: PendingIntent =
//            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//
//        val channelId = getString(R.string.app_name)
//        val defaultSoundUri =
//            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val notificationBuilder =
//            NotificationCompat.Builder(this, channelId)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("$channelId - Phản chiếu màn hình")
//                .setContentText("Quyền riêng tư của bạn đang được bảo v...")
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setPriority(10)
//                .setContentIntent(pendingIntent)
//                .setOngoing(true)
//
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Channel human readable title",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        notificationManager.notify(2, notificationBuilder.build())
    }
}