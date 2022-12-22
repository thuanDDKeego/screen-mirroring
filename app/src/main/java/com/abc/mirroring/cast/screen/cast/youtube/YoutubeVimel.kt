package com.abc.mirroring.cast.screen.cast.youtube

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseArray
import android.webkit.WebView
import androidx.core.util.keyIterator
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.abc.mirroring.cast.State
import com.abc.mirroring.cast.VimelStateHolder
import com.abc.mirroring.cast.section.Youtube
import com.abc.mirroring.cast.shared.cast.Caster
import com.abc.mirroring.cast.shared.cast.Command
import timber.log.Timber
import javax.inject.Inject


@SuppressLint("StaticFieldLeak")
@HiltViewModel
class YoutubeVimel @Inject constructor(
    @ApplicationContext val context: Context,
    var caster: Caster
) : VimelStateHolder<YoutubeVimel.YoutubeVimelState>(YoutubeVimelState()) {

    companion object {
        const val YOUTUBE_URL = "https://www.youtube.com/"
    }

    data class YoutubeVimelState(
        var url: String = YOUTUBE_URL,
        var ytbOptions: List<Youtube> = listOf(),
        var current: Youtube? = null
    ) : State

    private var webView: WebView? = null

    //if url is original (can't go back)
    fun isOriginalUrl() = !(webView?.canGoBack()?:true)
    private fun onUrlChanged(url: String) {
        update { state -> state.copy(url = url) }
    }

    private fun extract(url: String) {
        try {

            object : YouTubeExtractor(context) {
                override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, vMeta: VideoMeta?) {

                    if (vMeta == null) return
                    if (ytFiles == null) return

                    val ytbOptions = arrayListOf<Youtube>()

                    // more detail here: https://gist.github.com/sidneys/7095afe4da4ae58694d128b1034e01e2
                    for (iTag in ytFiles.keyIterator()) {
                        val ytb = ytFiles[iTag]
                        if (ytb != null && ytb.format.height >= 360 && ytb.format.audioBitrate != -1) {
                            ytb.let {
                                ytbOptions.add(
                                    Youtube(
                                        url = it.url,
                                        name = vMeta.title,
                                        duration = vMeta.videoLength * 1000, //to millis
                                        thumbnail = vMeta.sdImageUrl,
                                        format = it.format.height
                                    )
                                )
                            }
                        }
                    }

                    update { state -> state.copy(ytbOptions = ytbOptions, current = ytbOptions.last()) }
                }
            }.extract(url)

        } catch (e: Exception) {
            Timber.e("handleEventChangeVideoYoutube $e")
        }
    }

    fun onPageFinished(view: WebView?, url: String?) {
        Timber.d("onPageFinished $url $webView")
        webView = view
        if (url != null) {
            onUrlChanged(url)
            if (url.contains("watch?v=")) extract(url)
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

