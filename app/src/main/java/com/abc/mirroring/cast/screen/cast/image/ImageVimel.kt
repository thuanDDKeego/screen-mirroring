package com.abc.mirroring.cast.screen.cast.image

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.abc.mirroring.cast.State
import com.abc.mirroring.cast.VimelStateHolder
import com.abc.mirroring.cast.section.MediaPicker
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.section.Streamable
import net.sofigo.cast.tv.shared.cast.Command
import javax.inject.Inject

class ImageVimel @Inject constructor() : VimelStateHolder<ImageVimel.ImageVimelState>(
    ImageVimelState()
) {
    /**
     * STATE
     */
    data class ImageVimelState(
        //counter time click on button to show ads
        var counter: Int = 0,
        //index of current image in images
        var curIdx: Int = 0
    ) : State

    /**
     * Internal Data / Repository
     */
    var images: List<Streamable> by mutableStateOf(listOf())


    /**
     * == Function ==
     */

    fun fetch(context: Context, params: ImageParameter): List<Streamable> {
        /*internal -> query system
        **external -> get list url from params
        * */
        images = if (params.source == SourceType.Internal) {
            MediaPicker.images(context).reversed().toList()
        } else {
            params.urls ?: listOf()
        }
        return images
    }

    /**
     * Using to reset [ImageVimelState.counter]
     */
    fun reset() {
        update { state -> state.copy(counter = 0) }
    }

    /**
     * Using to add  [ImageVimelState.counter]
     */
    private fun count(offset: Int) {
        update { state -> state.copy(counter = state.counter + offset) }
    }

    /**
     * Using to update [ImageVimelState.curIdx]
     */
    fun moveTo(index: Int) {
        update { state -> state.copy(curIdx = minOf(maxOf(index, 0), images.size - 1)) }
    }

    fun moveTo(image: Streamable) {
        moveTo(images.indexOfFirst { it.id() == image.id() })
    }

    /**
     * get current selected image according to the current [ImageVimelState.curIdx]
     */
    fun current(): Streamable {
        return images[state.value.curIdx]
    }

    /**
     * Handle user behavior (on player controller)
     */
    fun onControl(command: Command) {
        val index = state.value.curIdx
        when (command) {
            is Command.Play -> moveTo(index).also { count(1) }
            is Command.Next -> moveTo(index.inc().run { if (this >= images.size) 0 else this }).also { count(1) }
            is Command.Previous -> moveTo(index.dec().run { if (this < 0) images.size else this }).also { count(1) }
            else -> {}
        }
    }
}