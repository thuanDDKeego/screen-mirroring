package com.abc.mirroring.cast.screen.cast.audible

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.abc.mirroring.cast.State
import com.abc.mirroring.cast.VimelStateHolder
import com.abc.mirroring.cast.screen.cast.image.ImageVimel.ImageVimelState
import com.abc.mirroring.cast.section.MediaPicker
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.section.Streamable
import com.abc.mirroring.cast.shared.cast.Caster
import com.abc.mirroring.cast.shared.cast.Command
import com.abc.mirroring.cast.shared.cast.SessionPlayer
import com.abc.mirroring.config.AppPreferences
import com.connectsdk.service.capability.MediaControl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AudibleVimel @Inject constructor(
    var caster: Caster
) : VimelStateHolder<AudibleVimel.AudibleVimelState>(
    AudibleVimelState()
) {

    data class AudibleVimelState(
        var index: Int = 0,
        var counter: Int = 0,
        var duration: Long = 0L,
        val mPosition: Long = 0,
        var isFinished: Boolean = false,
        var isPlaying: Boolean = false,
        var volume: Float = 0f,
        var isMute: Boolean = false,
        var current: Streamable? = null
    ) : State

    /**
     * refer to the media playlists from phone or internet depend on [AudibleParameter]
     */
    var playlists: List<Streamable> by mutableStateOf(listOf())
    // var current: Streamable? by mutableStateOf(null)

    /* current session controller */
    private var sPlayer: SessionPlayer? = null

    fun fetch(context: Context, params: AudibleParameter) {
        when (params.source) {
            SourceType.Internal -> {
                playlists = MediaPicker.audible(context, params.type)
            }

            SourceType.External -> {
                params.urls?.let { playlists = it }
            }
        }

        params.current?.let { moveTo(it) }
        fetchDuration()
    }

    fun cast(context: Context, streamable: Streamable) = caster.cast(streamable) {
        when (it) {
            is SessionPlayer.SessionStatus.Connected -> {
                /* update current variable */
                sPlayer = it.player // save current session object to send command to the TV
                /* listen to the player */
                onSessionStarted(sPlayer!!)
            }

            is SessionPlayer.SessionStatus.Error -> {
                Timber.e(it.error.stackTraceToString())
                it.error.message?.let { message ->
                    Toast.makeText(
                        context,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (!caster.isConnected()) {
                    caster.discovery.picker(context as Activity)
                }
            }
        }
    }

    /**
     * Using to reset [ImageVimelState.counter]
     */
    fun resetCounter() {
        update { state -> state.copy(counter = 0) }
    }


    /**
     * Using to add  [ImageVimelState.counter]
     */
    fun count(offset: Int) {
        update { state -> state.copy(counter = state.counter + offset) }
    }

    /**
     * Using to update [ImageVimelState.curIdx]
     */
    private fun moveTo(index: Int) {
        val idx = minOf(maxOf(index, 0), playlists.size - 1)
        update { state -> state.copy(index = idx, current = playlists[idx]) }
    }

    fun moveTo(audible: Streamable) {
        moveTo(playlists.indexOfFirst { it.id() == audible.id() })
    }

    private fun fetchDuration() {
        if (!needDuration()) update { state -> state.copy(duration = state.duration) }
    }

    private fun needDuration(): Boolean {
        state.value.current?.let { media ->
            if (media.source() == SourceType.Internal || media.mediaType() == MediaType.Youtube) {
                currentState.duration = state.value.current!!.duration().toLong()
                return false
            }
        }
        return true
    }

//    /**
//     * get current selected image according to the current [ImageVimelState.index]
//     */
//    fun current(): Streamable {
//        return playlists[state.value.index]
//    }

    /**
     * Handle user behavior (on player controller)
     */
    fun onControl(command: Command, onResponse: (Any?) -> Unit = {}) {
        val index = state.value.index
        // everytime user action -> count -> then show ads
        count(1)

        // process the command
        // TODO get state change it
        when (command) {
            is Command.Next -> moveTo(index.inc().run { if (this >= playlists.size) 0 else this })
            is Command.Previous -> moveTo(
                index.dec().run { if (this < 0) playlists.size else this })

            is Command.Mute -> sPlayer?.volume?.mute(!state.value.isMute)
            is Command.Pause, Command.Play -> {
                if (state.value.isPlaying) {
                    sPlayer?.media?.pause()
                    update { state -> state.copy(isPlaying = false) }
                } else {
                    sPlayer?.media?.play()
                    update { state -> state.copy(isPlaying = true) }
                }
            }

            is Command.VolumeDown -> sPlayer?.volume?.down()
            is Command.VolumeUp -> sPlayer?.volume?.up()
            is Command.Seek -> sPlayer?.media?.seek(command.position.toLong(), onResponse)
            is Command.Stop -> {
                sPlayer?.media?.stop()
            }

            else -> { /*missing replay10 and forward10*/
            }

        }
    }

    /**
     * When the file is casted to the device,
     * we want to listen all the state and update it to our phone UI
     *
     * this function mainly do that!
     */
    private var lastSessionJobs: Job? = null
    private fun onSessionStarted(player: SessionPlayer) {
        update { state ->
            state.copy(
                volume = player.volume.current.value ?: 0f,
                isMute = player.volume.mute.value,
                mPosition = 0
            )
        }

        lastSessionJobs?.cancel()
        lastSessionJobs = viewModelScope.launch {
            launch {
                player.media.status.collect {
                    it?.let {
                        update { state -> state.copy(isPlaying = it == MediaControl.PlayStateStatus.Playing) }

                        if (!state.value.isFinished) {
                            AppPreferences().countSatisfied = AppPreferences().countSatisfied!! + 1
                            update { state -> state.copy(isFinished = it == MediaControl.PlayStateStatus.Finished) }
                        }
                    }

                }
            }
            //kiểm tra xem có thực sự cần lấy duration từ sdk không?
            if (needDuration()) {
                launch {
                    /*vì hàm media.duration() cần 1 thời gian sau khi video chạy mới load được nên
                ta xử lý bằng cách dùng while loop run cho đến khi duration > 0, kể cả while loop
                vô hạn thì cũng đã để trong view-modelScope
                * */
                    while (state.value.duration == 0L) {
                        player.media.duration().let {
                            //check if while loop is running ...
                            Timber.v("while loop get duration")
                            if (it != null && it > 0L) {
                                update { state ->
                                    Timber.v("duration updated: $it")
                                    state.copy(duration = it)
                                }
                            }
                        }
                        delay(1000)
                    }
                }
            }
            launch {
                player.media.positions().collect {
                    it?.let {
                        update { state ->
                            state.copy(mPosition = it)
                        }
                    }
                }
            }
            launch {
                player.volume.mute.collect {
                    update { state -> state.copy(isMute = it) }
                }
            }
            launch {
                player.volume.current.collect {
                    it?.let { update { state -> state.copy(volume = it) } }
                }
            }
        }
    }
}