package com.abc.mirroring.cast.screen.cast.iptv

import android.annotation.SuppressLint
import android.content.Context
import com.abc.mirroring.cast.State
import com.abc.mirroring.cast.VimelStateHolder
import com.abc.mirroring.cast.section.M3U8File
import com.abc.mirroring.cast.shared.cast.Caster
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class IPTVVimel @Inject constructor(
    @ApplicationContext val context: Context,
    var caster: Caster
) : VimelStateHolder<IPTVVimel.IPTVVimelState>(IPTVVimelState()) {

    companion object {
    }

    data class IPTVVimelState(
        var currentM3U: M3UItem? = null,
        var m3us: List<M3UItem> = listOf(),
        var isLoading: Boolean = false,
        var channels: List<M3U8File> = listOf()
    ) : State

    fun fetchChannels() {
        update { state -> state.copy(isLoading = true) }
        getChannels {
            update { state -> state.copy(channels = it) }
        }
    }

    private fun getChannels(onResult: (List<M3U8File>) -> Unit) {
        if (state.value.currentM3U == null) {
            update { state -> state.copy(isLoading = false) }
            onResult(listOf())
            return
        }
        IPTVExtractor.getChannels(state.value.currentM3U!!.url) {
            update { state -> state.copy(isLoading = false) }
            onResult(it)
        }
    }

    fun updateCurrentM3U(item: M3UItem) {
        update { state -> state.copy(currentM3U = item) }
    }

    fun addM3U(item: M3UItem) {
        update { state -> state.copy(m3us = state.m3us.toMutableList().also { it.add(0, item) }) }
    }

    fun resetChannels() {
        update {state -> state.copy(channels = listOf())}
    }

    fun resetState() {
        update {state -> state.copy(channels = listOf())}
        update {state -> state.copy(isLoading = false)}
    }
}