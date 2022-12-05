package com.abc.mirroring.cast.shared.ui.component

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun intercepted_browser(
    modifier: Modifier = Modifier,
    url: String,
    onPageFinished: (view: WebView?, url: String?) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                        onPageFinished(view, url)
                        super.doUpdateVisitedHistory(view, url, isReload)
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