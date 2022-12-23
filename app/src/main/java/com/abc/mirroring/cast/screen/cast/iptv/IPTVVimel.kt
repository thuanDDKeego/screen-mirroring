package com.abc.mirroring.cast.screen.cast.iptv

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.abc.mirroring.cast.State
import com.abc.mirroring.cast.VimelStateHolder
import com.abc.mirroring.cast.section.M3U8File
import com.abc.mirroring.cast.section.data.iptv.M3U
import com.abc.mirroring.cast.shared.cast.Caster
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class IPTVVimel @Inject constructor(
    @ApplicationContext val context: Context,

    var caster: Caster
) : VimelStateHolder<IPTVVimel.IPTVVimelState>(IPTVVimelState()) {

    @Inject
    lateinit var iptvRepository: IPTVRepository

    data class IPTVVimelState(
        var currentM3U: M3U? = null,
        var m3us: List<M3U> = listOf(),
        var m3uWantToDelete: M3U? = null,
        var m3uWantToUpdate: M3U? = null,
        var isLoading: Boolean = false,
        var channels: List<M3U8File> = listOf()
    ) : State

    fun fetchM3Us() {
        viewModelScope.launch {
            update {state -> state.copy(isLoading = true)}
            //handle data should be run on IO
            withContext(Dispatchers.IO) {
                val m3us = iptvRepository.getAllM3U()
                //update ui must be run on Main
                withContext(Dispatchers.Main) {
                    //loading complete
                    update {state -> state.copy(isLoading = false)}
                    //update m3us
                    update { state -> state.copy(m3us = m3us) }
                }
            }
        }
    }

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

    fun updateCurrentM3U(item: M3U) {
        update { state -> state.copy(currentM3U = item) }
    }

    fun addM3U(item: M3U) {
        viewModelScope.launch {
            //handle data should be run on IO
            withContext(Dispatchers.IO) {
                iptvRepository.addM3U(item)
                //after modify db, need refresh data
                fetchM3Us()
            }
        }
//        update { state -> state.copy(m3us = state.m3us.toMutableList().also { it.add(0, item) }) }
    }

    //m3u file you want to delete
    fun updateM3uWantToDelete(m3u: M3U) {
        update {state -> state.copy(m3uWantToDelete =  m3u)}
    }

    //m3u file you want to update
    fun updateM3uWantToUpdate(m3u: M3U) {
        update {state -> state.copy(m3uWantToUpdate =  m3u)}
    }

    //delete m3u from db
    fun delete(item: M3U) {
        viewModelScope.launch {
            //handle data should be run on IO
            withContext(Dispatchers.IO) {
                iptvRepository.delete(item)
                //reset m3u want to delete
                update {state -> state.copy(m3uWantToDelete =  null)}
                //after modify db, need refresh data
                fetchM3Us()
            }
        }
    }

    //update m3u from db
    fun update(item: M3U) {
        viewModelScope.launch {
            //handle data should be run on IO
            withContext(Dispatchers.IO) {
                iptvRepository.update(item)
                //reset m3u want to update
                update {state -> state.copy(m3uWantToUpdate =  null)}
                //after modify db, need refresh data
                fetchM3Us()
            }
        }
    }

    fun resetState() {
        update {state -> state.copy(channels = listOf())}
        update {state -> state.copy(isLoading = false)}
    }
}