package com.abc.mirroring.cast.screen.cast.web

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import com.abc.mirroring.cast.State
import com.abc.mirroring.cast.VimelStateHolder
import com.abc.mirroring.cast.section.WebMedia
import com.abc.mirroring.cast.shared.cast.Caster
import com.abc.mirroring.cast.shared.cast.Command
import com.abc.mirroring.cast.shared.cast.webcast.WebViewExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
        WebViewExtractor.toWebMedia(url)?.let { media ->
//            mutableListOf<WebMedia>()
//                .also { it.add(media) }
//                .also { it.addAll(state.value.medias) }
            update { state ->
                state.copy(
                    medias = state.medias.toMutableList().also { it.add(0, media) })
            }
        }

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
}

