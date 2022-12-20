//package com.abc.mirroring.cast.shared.cast.webcast
//
//import android.annotation.SuppressLint
//import android.webkit.JavascriptInterface
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import com.abc.mirroring.cast.section.MediaType
//import com.abc.mirroring.cast.section.WebMedia
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.filter
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.launch
//import org.jsoup.Jsoup
//import timber.log.Timber
//import java.io.IOException
//
//object WebViewExtractor {
//
//
//    suspend fun streamableFromUrl(webView: WebView): Flow<WebMedia> {
//        getLinks(webView)
//        val s = CoroutineScope(Dispatchers.Main).async {
//            getLinks(webView)
//        }
//        //when done
//
//        return urlFlow
//            .filter { it.isMediaLink() }
//            .map { url ->
//                url.toWebMedia()
//            }
//    }
//
////    @SuppressLint("JavascriptInterface")
////    private fun getLinks(webView: WebView) {
////        webView.apply {
////            addJavascriptInterface(JavascriptInterface(), "HTMLOUT")
////            webViewClient = object : WebViewClient() {
////                override fun onPageFinished(view: WebView?, url: String?) {
////                    super.onPageFinished(view, url)
////                }
////
////                override fun onLoadResource(view: WebView?, url: String?) {
////                    super.onLoadResource(view, url)
////                    Timber.d("onLoadResource $url")
////                    url?.let {
////                        CoroutineScope(Dispatchers.Default).launch {
////                            urlFlow.emit(it)
////                        }
////                    }
////                }
////            }
////        }
////    }
//
//    private fun getLinks(url: String): List<String> {
//        val links = mutableListOf<String>()
//        links.add(url.trim())
//        try {
//            val doc = Jsoup.connect(url).get()
//            /*hash map này có key dùng để select ra các element, và value lấy ra thuộc tính(attribute)
//            example:
//                        val listLink = doc.select("a[href]")
//                        listLink.forEach() {
//                            it.attr("abs:href")     ---> trả về url
//                        }
//            */
//            hashMapOf(
//                "a[href]" to "abs:href",
//                "[src]" to "abs:src",
//                "link[href]" to "abs:href"
//            ).forEach { (tag, attr) ->
//                links.addAll(doc.select(tag).map { ele -> ele.attr(attr) })
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        //return list url
//        return links
//    }
//
////    private fun print(msg: String, vararg args: Any) {
////        println(String.format(msg, *args))
////    }
//
////    private fun trim(s: String, width: Int): String {
////        return if (s.length > width) s.substring(0, width - 1) + "." else s
////    }
//
//    private fun String.isMediaLink(): Boolean {
//        return this.let { str ->
//            val etx: String = str.split(".").last()
//            //this is full extension
////            etx == "mp4" || etx == "flv" || etx == "m4a" || etx == "3gp" || etx == "mkv" ||
////                    etx == "mp3" || etx == "ogg" || etx == "jpg" || etx == "png" || etx == "gif"
//            //this is extension that is supported by connect-sdk
//            etx == "mp4" || etx == "mp3" || etx == "png" || etx == "jpg"
//        }
//    }
//
//    private fun String.toWebMedia(): WebMedia {
//        return this.let { url ->
//            val etx: String = url.split(".").last()
//            val mediaType = when (etx.lowercase()) {
//                "mp4", "flv", "m4a", "3gp", "mkv" -> MediaType.Video
//                "jpg", "png", "gif" -> MediaType.Image
//                else -> MediaType.Audio
//            }
//            WebMedia(url, url, 0L, mediaType, etx)
//        }
//    }
//
//    private fun List<String>.mediaLinks(): List<String> {
//        return this.filter { str ->
//            val etx: String = str.split(".").last()
//            //this is full extension
////            etx == "mp4" || etx == "flv" || etx == "m4a" || etx == "3gp" || etx == "mkv" ||
////                    etx == "mp3" || etx == "ogg" || etx == "jpg" || etx == "png" || etx == "gif"
//            //this is extension that is supported by connect-sdk
//            etx == "mp4" || etx == "mp3" || etx == "png" || etx == "jpg"
//        }
//    }
//
//    private fun List<String>.toWebMedias(): List<WebMedia> {
//        return this.map { url ->
//            val etx: String = url.split(".").last()
//            val mediaType = when (etx.lowercase()) {
//                "mp4", "flv", "m4a", "3gp", "mkv" -> MediaType.Video
//                "jpg", "png", "gif" -> MediaType.Image
//                else -> MediaType.Audio
//            }
//            WebMedia(url, url, 0L, mediaType, etx)
//        }
//    }
//}