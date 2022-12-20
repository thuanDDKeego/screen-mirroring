package com.abc.mirroring.cast.screen.cast.web

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import com.abc.mirroring.cast.State
import com.abc.mirroring.cast.VimelStateHolder
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.WebMedia
import com.abc.mirroring.cast.shared.cast.Caster
import com.abc.mirroring.cast.shared.cast.Command
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@SuppressLint("StaticFieldLeak")
@HiltViewModel
class WebVimel @Inject constructor(
    @ApplicationContext val context: Context,
    var caster: Caster
) : VimelStateHolder<WebVimel.WebVimelState>(WebVimelState()) {
    companion object {
        const val GOOGLE_URL = "https://www.google.com/"
        const val SEARCH_URL = "https://www.google.com/search?q="
    }

    data class WebVimelState(
        var url: String = GOOGLE_URL,
        var medias: List<WebMedia> = listOf(),
    ) : State

    private var webView: WebView? = null

    //if url is original (can't go back) -> navigateUp
    fun isOriginalUrl() = !(webView?.canGoBack() ?: true)

    fun search(query: String) {
        webView?.loadUrl("$SEARCH_URL$query")
    }

    private fun onUrlChanged(url: String) {
        update { state -> state.copy(url = url) }
        update { state -> state.copy(medias = listOf()) }
    }

//    private fun extract() {
//        viewModelScope.launch {
//            webView?.let {
//                WebViewExtractor.streamableFromUrl(it).let {
//                    it.collect { media ->
//                        val newMedias =
//                            mutableListOf<WebMedia>()
//                                .also { it.add(media) }
//                                .also { it.addAll(state.value.medias) }
//
//                        update { state -> state.copy(medias = newMedias) }
//                    }
//                }
//            }
//        }
//    }

    fun onLoadResource(url: String) {
        if (!url.isMediaLink()) return
        val media = url.toWebMedia()
        val newMedias =
            mutableListOf<WebMedia>()
                .also { it.add(media) }
                .also { it.addAll(state.value.medias) }
        update { state -> state.copy(medias = newMedias) }
    }

    fun onPageFinished(view: WebView?, url: String?) {
        Timber.d("onPageFinished $url $webView")
        webView = view

        if (url != null) {
            onUrlChanged(url)
//            webView?.let { extract() }
        }
    }

    fun onControl(command: Command, onResponse: (Any?) -> Unit = {}) {
        when (command) {
            is Command.Previous -> webView?.goBack()
            is Command.Next -> webView?.goForward()
            else -> {}
        }
    }

    private fun String.isMediaLink(): Boolean {
        return this.let { str ->
            val etx: String = str.split(".").last()
            //this is full extension
//            etx == "mp4" || etx == "flv" || etx == "m4a" || etx == "3gp" || etx == "mkv" ||
//                    etx == "mp3" || etx == "ogg" || etx == "jpg" || etx == "png" || etx == "gif"
            //this is extension that is supported by connect-sdk
            etx == "mp4" || etx == "mp3" || etx == "png" || etx == "jpg"
        }
    }

    private fun String.toWebMedia(): WebMedia {
        return this.let { url ->
            val etx: String = url.split(".").last()
            val mediaType = when (etx.lowercase()) {
                "mp4", "flv", "m4a", "3gp", "mkv" -> MediaType.Video
                "jpg", "png", "gif" -> MediaType.Image
                else -> MediaType.Audio
            }
            WebMedia(url, url, 0L, mediaType, etx)
        }
    }
}

