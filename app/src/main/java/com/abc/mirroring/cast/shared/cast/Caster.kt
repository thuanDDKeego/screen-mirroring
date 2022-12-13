package com.abc.mirroring.cast.shared.cast

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.section.Streamable
import com.abc.mirroring.cast.shared.cast.SessionPlayer.SessionStatus.Error
import com.abc.mirroring.cast.shared.utils.FileUtils
import com.abc.mirroring.cast.shared.utils.NetworkUtils
import com.connectsdk.core.MediaInfo
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.service.capability.MediaPlayer
import com.connectsdk.service.capability.listeners.ResponseListener
import com.connectsdk.service.command.ServiceCommandError
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CORS
import io.ktor.features.PartialContent
import io.ktor.request.httpMethod
import io.ktor.response.respondFile
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class Caster(
    private val ctx: Context
) {

    /**
     * Using for discover new device
     */
    val discovery = DeviceDiscovery(ctx)

    /**
     * this file will be used as video/image that stream to TV
     * it will be updated by user by using [cast]
     * @private
     */
    private val file = MutableStateFlow<File>(File(""))
    private var currentSession: SessionPlayer? = null
    private var isTryingToDisplay = false


    private val server by lazy {
        embeddedServer(CIO, 6996) {
            install(PartialContent)
            install(AutoHeadResponse)
            install(CORS) {
                allowCredentials = true
                allowHeaders { true }
                anyHost()
//                HttpMethod.DefaultMethods.forEach { allowHeaders { it.value -> true} }
            }
            routing {
                get("/") {
                    file.collectLatest {
                        Timber.v("Response ${call.request.httpMethod} $it")
                        call.respondFile(it)
                    }
                }
            }
        }
    }

    fun start() {
        Timber.i("Starting Carter at 6996")
        // Start server
        CoroutineScope(Dispatchers.IO).launch {
            server.start(wait = true)

            file.collect {
                // TODO: update session everytime file changed
            }
        }
        Timber.d(String.format("Start at: %s:%d", NetworkUtils.getLocalIpAddress(), 6996))
    }

    fun shutdown() {
        discovery.stop()
        server.stop(1_000, 2_000)
    }

    fun mirror(context: Context, onFailed: (e: Exception) -> Unit) {
        try {
            context.startActivity(Intent("android.settings.WIFI_DISPLAY_SETTINGS"))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            try {
                context.startActivity(Intent("com.samsung.wfd.LAUNCH_WFD_PICKER_DLG"))
            } catch (e2: java.lang.Exception) {
                try {
                    context.startActivity(Intent("android.settings.CAST_SETTINGS"))
                } catch (e3: java.lang.Exception) {
                    onFailed(e)
                }
            }
        }
    }

    /**
     * trying to stream anyway if the device connected or not
     */
    fun cast(media: Streamable, onResponse: (SessionPlayer.SessionStatus) -> Unit) {
        val device = discovery.device.value
        if (device != null && device.isConnected) {
            cast(device, media, onResponse)
        } else {
            onResponse(Error(Throwable("Device didn't connected")))
        }

    }


    private fun isSameDevice(device: ConnectableDevice): Boolean {
        if (currentSession == null) return false;
        return device.id === currentSession!!.device.id // same id
                && device.services.contains(currentSession!!.launcher.launchSession.service) //same protocol
    }

    /**
     * Test function
     * Only work for videos only
     * https://connectsdk.com/en/latest/guide-and/and-beam-media.html
     */
    fun cast(device: ConnectableDevice, media: Streamable, onResponse: (SessionPlayer.SessionStatus) -> Unit) {
        if (!isSameDevice(device) // user change TV
            || (currentSession != null && !currentSession!!.device.isConnected)// user disconnect current tV
        ) {
            isTryingToDisplay = false
            currentSession?.clear()
            currentSession = null
        }


        if (isTryingToDisplay && isSameDevice(device)) {
            onResponse(Error(Throwable("Device is trying to connect to TV, please wait!")))
            return
        }
        // launch on coroutine for safe
        CoroutineScope(Dispatchers.IO).launch {
            val url = if (media.source() === SourceType.Internal) {
                FileUtils.getMediaPath(ctx, media.uri()!!)
                    ?.also {
                        file.value = File(it)
                        Timber.d("Assign new file to server: $file")
                    }
                    ?: return@launch onResponse(Error(Throwable("File doesn't exists")))

                String.format("http://%s:%d", NetworkUtils.getLocalIpAddress(), 6996)
            } else {
                media.url()
            }

            // TODO: getMimeType co the dang con bugs
            val mimeType = media.mineType(ctx)

            Timber.d("Casting $mimeType $url")

            val mediaInfo: MediaInfo = MediaInfo.Builder(url, mimeType)
                .setTitle(media.name())
                .setDescription("Enjoy! Rate us 5 stars. it will help us in making better services") // TODO: Change by appstore name
                .build()

            val listener: MediaPlayer.LaunchListener = object : MediaPlayer.LaunchListener {
                override fun onSuccess(launcher: MediaPlayer.MediaLaunchObject) {
                    Timber.i("Launched new session $launcher")
                    isTryingToDisplay = false
                    currentSession = SessionPlayer(device, launcher, discovery.volumeController!!)
                    onResponse(SessionPlayer.SessionStatus.Connected(currentSession!!))
                }

                override fun onError(error: ServiceCommandError) {
                    Timber.e(error.message + " " + error.payload)
                    isTryingToDisplay = false
                    onResponse(Error(error))
                }
            }


            try {
                // firstly, flag trying to display
                isTryingToDisplay = true

                /**
                 * if this is first time stream, launch it
                 */
                if (currentSession == null || !isSameDevice(device)) {
                    currentSession?.clear()
                    currentSession = null
                    device.getCapability(MediaPlayer::class.java)?.playMedia(mediaInfo, false, listener)
                    return@launch
                }


                /**
                 * if we have connected to TV and want to change to the next video
                 */
                device.getCapability(MediaPlayer::class.java)?.closeMedia(currentSession?.launcher?.launchSession, object : ResponseListener<Any> {
                    override fun onError(error: ServiceCommandError) {
                        Timber.e("Close media: " + error?.message + " " + error?.payload)
                        device.getCapability(MediaPlayer::class.java)?.playMedia(mediaInfo, false, listener)
                    }

                    override fun onSuccess(temp: Any) {
                        Timber.v("Close media success!")
                        device.getCapability(MediaPlayer::class.java)?.playMedia(mediaInfo, false, listener)
                    }
                })

            } catch (e: Exception) {
                onResponse(Error(e))
            }
        }
    }

    fun isConnected(): Boolean {
        return this.discovery.device.value?.isConnected ?: false
    }

    fun disconnect() {
        this.discovery.device.value?.disconnect()
    }

    fun device(): StateFlow<ConnectableDevice?> {
        return this.discovery.device
    }
}
