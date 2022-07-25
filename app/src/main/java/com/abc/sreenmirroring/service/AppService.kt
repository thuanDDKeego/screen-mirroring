package com.abc.sreenmirroring.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.abc.sreenmirroring.config.AppPreferences
import com.abc.sreenmirroring.service.helper.IntentAction
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.dvkr.screenstream.data.model.AppError
import info.dvkr.screenstream.data.model.FatalError
import info.dvkr.screenstream.data.other.getLog
import info.dvkr.screenstream.data.settings.Settings
import info.dvkr.screenstream.data.settings.SettingsImpl
import info.dvkr.screenstream.data.settings.SettingsReadOnly
import info.dvkr.screenstream.data.state.AppStateMachine
import info.dvkr.screenstream.data.state.AppStateMachineImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class AppService : Service() {

    companion object {
        var isRunning: Boolean = false

        fun getAppServiceIntent(context: Context): Intent = Intent(context.applicationContext, AppService::class.java)

        fun startService(context: Context, intent: Intent) = context.startService(intent)

        fun startForeground(context: Context, intent: Intent) = ContextCompat.startForegroundService(context, intent)
    }

    class AppServiceBinder(private val serviceMessageSharedFlow: MutableSharedFlow<ServiceMessage>) : Binder() {
        fun getServiceMessageFlow(): SharedFlow<ServiceMessage> = serviceMessageSharedFlow.asSharedFlow()
    }

    private val _serviceMessageSharedFlow =
        MutableSharedFlow<ServiceMessage>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private var appServiceBinder: AppServiceBinder? = AppServiceBinder(_serviceMessageSharedFlow)

    private fun sendMessageToActivities(serviceMessage: ServiceMessage) {
        _serviceMessageSharedFlow.tryEmit(serviceMessage)
    }

    private val coroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate + CoroutineExceptionHandler { _, throwable ->
            onError(FatalError.CoroutineException)
        }
    )

    private val isStreaming = AtomicBoolean(false)
    private val errorPrevious = AtomicReference<AppError?>(null)

    override fun onBind(intent: Intent?): IBinder? {
        return appServiceBinder
    }

    private fun onError(appError: AppError?) {
        val oldError = errorPrevious.getAndSet(appError)
        oldError != appError || return
    }

    private suspend fun onEffect(effect: AppStateMachine.Effect) = coroutineScope.launch {
        if (effect !is AppStateMachine.Effect.Statistic)

        when (effect) {
            is AppStateMachine.Effect.ConnectionChanged -> Unit  // TODO Notify user about restart reason

            is AppStateMachine.Effect.PublicState -> {
                isStreaming.set(effect.isStreaming)

                sendMessageToActivities(
                    ServiceMessage.ServiceState(
                        effect.isStreaming, effect.isBusy, effect.isWaitingForPermission,
                        effect.netInterfaces, effect.appError
                    )
                )

                onError(effect.appError)
            }

            is AppStateMachine.Effect.Statistic ->
                when (effect) {
                    is AppStateMachine.Effect.Statistic.Clients -> {
                        sendMessageToActivities(ServiceMessage.Clients(effect.clients))
                    }

                    is AppStateMachine.Effect.Statistic.Traffic ->
                        sendMessageToActivities(ServiceMessage.TrafficHistory(effect.traffic))

                    else -> throw IllegalArgumentException("Unexpected onEffect: $effect")
                }
        }
    }.join()

    private lateinit var settings: Settings

    private var appStateMachine: AppStateMachine? = null

    override fun onCreate() {
        super.onCreate()
        settings = SettingsImpl(
            BinaryPreferencesBuilder(applicationContext)
                .supportInterProcess(true)
                .exceptionHandler { ex -> Timber.e(ex) }
                .build()
        )
        settings.autoChangePinOnStart()
        settings.enablePin = AppPreferences().isTurnOnPinCode == true
        settings.pin = AppPreferences().pinCode.toString()

        appStateMachine = AppStateMachineImpl(this, settings as SettingsReadOnly, ::onEffect)

        isRunning = true
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intentAction = IntentAction.fromIntent(intent)
        intentAction != null || return START_NOT_STICKY

        when (intentAction) {
            IntentAction.GetServiceState -> {
                appStateMachine?.sendEvent(AppStateMachine.Event.RequestPublicState)
            }

            IntentAction.StartStream -> {
                Timber.d("StartStream")
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                    sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                appStateMachine?.sendEvent(AppStateMachine.Event.StartStream)
            }

            IntentAction.StopStream -> {
                Timber.d("StopStream ")
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                    sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                appStateMachine?.sendEvent(AppStateMachine.Event.StopStream)
            }

            IntentAction.Exit -> {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                    sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                stopForeground(true)
                sendMessageToActivities(ServiceMessage.FinishActivity)
                this@AppService.stopSelf()
            }

            is IntentAction.CastIntent -> {
                appStateMachine?.sendEvent(AppStateMachine.Event.RequestPublicState)
                appStateMachine?.sendEvent(AppStateMachine.Event.StartProjection(intentAction.intent))
            }

            IntentAction.CastPermissionsDenied -> {
                appStateMachine?.sendEvent(AppStateMachine.Event.CastPermissionsDenied)
                appStateMachine?.sendEvent(AppStateMachine.Event.RequestPublicState)
            }

            IntentAction.StartOnBoot ->
                appStateMachine?.sendEvent(AppStateMachine.Event.StartStream, 4500)

            IntentAction.RecoverError -> {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                    sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                appStateMachine?.sendEvent(AppStateMachine.Event.RecoverError)
            }
            else -> {}
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        runBlocking(coroutineScope.coroutineContext) { withTimeout(2000) { appStateMachine?.destroy() } }
        appStateMachine = null
        coroutineScope.cancel(CancellationException("AppService.destroy"))
        stopForeground(true)
        appServiceBinder = null
        super.onDestroy()
    }

}