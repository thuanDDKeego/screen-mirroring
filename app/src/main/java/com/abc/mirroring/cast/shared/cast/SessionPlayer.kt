package com.abc.mirroring.cast.shared.cast

import com.connectsdk.device.ConnectableDevice
import com.connectsdk.service.capability.MediaControl
import com.connectsdk.service.capability.MediaPlayer
import com.connectsdk.service.capability.VolumeControl
import com.connectsdk.service.capability.listeners.ResponseListener
import com.connectsdk.service.command.ServiceCommandError
import com.connectsdk.service.command.ServiceSubscription
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SessionPlayer(
    val device: ConnectableDevice,
    val launcher: MediaPlayer.MediaLaunchObject,
    private val volumeController: VolumeController
) {

    sealed class SessionStatus {
        data class Connected(val player: SessionPlayer) : SessionStatus()
        data class Error(val error: Throwable) : SessionStatus()
    }

    val media = MediaController()
    val volume = volumeController

    //TODO check cause crash
    inner class MediaController {
        var status = MutableStateFlow<MediaControl.PlayStateStatus?>(null)
        var subscriptions: MutableList<ServiceSubscription<out ResponseListener<*>>> = mutableListOf()

        init {
            launcher.mediaControl?.subscribePlayState(object : MediaControl.PlayStateListener {
                override fun onError(error: ServiceCommandError?) {
                }

                override fun onSuccess(data: MediaControl.PlayStateStatus?) {
                    status.value = data
                }
            })?.let { subscriptions.add(it) }
        }

        /**
         * listen to position of current media
         * @caution remember to stop it
         */
        fun positions(): Flow<Long?> = flow {
            while (true) {
                if (status.value == MediaControl.PlayStateStatus.Playing) {
                    emit(position())
                }
                delay(100L)
            }
        }

        /**
         * Return the current media duration
         */
        suspend fun duration() = suspendCoroutine {
            launcher.mediaControl.getDuration(object : MediaControl.DurationListener {
                override fun onError(error: ServiceCommandError?) {
                    it.resume(null)
                }

                override fun onSuccess(data: Long?) {
                    it.resume(data)
                }
            })
        }

        /**
         * Return the current position
         */
        suspend fun position() = suspendCoroutine {
            launcher.mediaControl.getPosition(object : MediaControl.PositionListener {
                override fun onError(error: ServiceCommandError?) {
                    it.resume(null)
                }

                override fun onSuccess(data: Long?) {
                    it.resume(data)
                }
            })
        }

        /**
         * TODO: delete this function
         */
        @Deprecated("will deleted later")
        fun playOrPause() {
            try {
                if (status.value == MediaControl.PlayStateStatus.Playing) {
                    launcher.mediaControl?.pause(null)
                } else {
                    launcher.mediaControl?.play(null)
                }
            } catch (_: Exception) {
            }
        }

        fun pause() {
            if (status.value == MediaControl.PlayStateStatus.Paused) return
            launcher.mediaControl?.pause(null)
        }

        fun play() {
            if (status.value == MediaControl.PlayStateStatus.Playing) return
            launcher.mediaControl?.play(null)
        }

        /**
         * TODO: handle error when stop
         */
        fun stop(callback: () -> Unit = {}) {
            launcher.mediaControl?.stop(object : ResponseListener<Any> {
                override fun onError(error: ServiceCommandError?) {
                    callback.invoke()
                }

                override fun onSuccess(`object`: Any?) {
                    callback.invoke()
                }
            })
        }

        fun seek(time: Long, onResponse: (Any?) -> Unit) {
            try {
                launcher.mediaControl?.seek(time, object : ResponseListener<Any> {
                    override fun onError(error: ServiceCommandError?) {
                        onResponse(error)
                    }

                    override fun onSuccess(response: Any?) {
                        onResponse(response)
                    }
                })
            } catch (_: Exception) {
            }
        }

    }

    /**
     * @coreObject
     * Volume Control Object
     * The lifecycle of this object depend on parent class [SessionPlayer]
     */
    class VolumeController(val device: ConnectableDevice) {
        var current = MutableStateFlow<Float?>(null)
        var mute = MutableStateFlow(false)

        var subscriptions: MutableList<ServiceSubscription<out ResponseListener<*>>> = mutableListOf()

        init {
            Timber.i("Subscribe to volume listener")

            device.getCapability(VolumeControl::class.java)?.let {
                subscriptions.add(it.subscribeVolume(object : VolumeControl.VolumeListener {
                    override fun onError(error: ServiceCommandError?) {
                        Timber.e("Volume: $error")

                    }

                    override fun onSuccess(data: Float?) {
                        Timber.v("Volume: $data")
                        current.value = data
                    }

                }))

                subscriptions.add(it.subscribeMute(object : VolumeControl.MuteListener {
                    override fun onError(error: ServiceCommandError?) {
                        Timber.e("Mute: $error")
                    }

                    override fun onSuccess(data: Boolean?) {
                        Timber.v("Mute: $data")
                        mute.value = data ?: false
                    }

                }))
            }
        }

        fun mute(value: Boolean) {
            device.getCapability(VolumeControl::class.java).setMute(value, null)
        }

        fun down() {
            Timber.v("Command down")
            current.value?.let {
                device.getCapability(VolumeControl::class.java)?.setVolume((it - 0.05f).coerceAtLeast(0f), object : ResponseListener<Any> {
                    override fun onError(error: ServiceCommandError?) {
                        Timber.e("Volume Down: $error")
                    }

                    override fun onSuccess(data: Any?) {
                        Timber.v("Volume Down: $data")
                    }
                })
            }
        }

        fun up() {
            if (mute.value) {
                // if volume is muted -> turn it on
                mute(false)
            } else {
                Timber.v("Command up")
                // turn up volume
                current.value?.let {
                    device.getCapability(VolumeControl::class.java)?.setVolume((it + 0.05f).coerceAtMost(1f), object : ResponseListener<Any> {
                        override fun onError(error: ServiceCommandError?) {
                            Timber.e("Volume Up: $error")
                        }

                        override fun onSuccess(data: Any?) {
                            Timber.v("Volume up: $data")
                        }
                    })
                }
            }
        }
    }

    fun clear() {
        media.subscriptions.forEach { it.unsubscribe() }
//        volume.subscriptions.forEach { it.unsubscribe() }
        launcher.mediaControl?.stop(null)
        launcher.launchSession.close(null)
    }

}