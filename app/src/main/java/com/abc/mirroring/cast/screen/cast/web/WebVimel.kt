package com.abc.mirroring.cast.screen.cast.web

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import com.abc.mirroring.cast.State
import com.abc.mirroring.cast.VimelStateHolder
import com.abc.mirroring.cast.section.WebMedia
import com.abc.mirroring.cast.shared.cast.Caster
import net.sofigo.cast.tv.shared.cast.Command
import com.abc.mirroring.cast.shared.cast.webcast.WebViewExtractor
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
    }

    private fun extract(url: String) {
        viewModelScope.launch {
            WebViewExtractor.streamableFromUrl(url).let {
                update { state ->
                    state.copy(medias = it)
                }
            }
        }
    }

    fun onPageFinished(view: WebView?, url: String?) {
        Timber.d("onPageFinished $url $webView")
        webView = view
        if (url != null) {
            onUrlChanged(url)
            extract(url)
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

