package com.abc.mirroring.cast.shared.ui.component

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

@Composable
fun intercepted_browser(
    modifier: Modifier = Modifier,
    url: String,
    onLoadResource: ((String) -> Unit)? = null,
onPageFinished: (view: WebView?, url: String?) -> Unit
) {
    val urlFlow: MutableStateFlow<String> = MutableStateFlow("")
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun doUpdateVisitedHistory(
                        view: WebView?,
                        url: String?,
                        isReload: Boolean
                    ) {
                        onPageFinished(view, url)
                        super.doUpdateVisitedHistory(view, url, isReload)
                    }

                    override fun onLoadResource(view: WebView?, url: String?) {
                        super.onLoadResource(view, url)
                        Timber.d("onLoadResource $url")
                        url?.let {
                            onLoadResource?.invoke(url)
                        }
                    }
                }
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        },
        update = {
            // it.loadUrl(url)
        })
}