package com.abc.mirroring.cast.shared.cast.webcast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.WebMedia
import org.jsoup.Jsoup
import java.io.IOException

object WebViewExtractor {
    suspend fun streamableFromUrl(url: String): List<WebMedia> {
        // TODO show Progress Dialog
        val s = CoroutineScope(Dispatchers.Default).async {
            getLinks(url)
        }

        //when done
        return s.await().mediaLinks().toWebMedias()
    }

    private fun getLinks(url: String): List<String> {
        val links = mutableListOf<String>()
        links.add(url.trim())
        try {
            val doc = Jsoup.connect(url).get()
            /*hash map này có key dùng để select ra các element, và value lấy ra thuộc tính(attribute)
            example:
                        val listLink = doc.select("a[href]")
                        listLink.forEach() {
                            it.attr("abs:href")     ---> trả về url
                        }
            */
            hashMapOf("a[href]" to "abs:href", "[src]" to "abs:src", "link[href]" to "abs:href").forEach { (tag, attr) ->
                links.addAll(doc.select(tag).map { ele -> ele.attr(attr) })
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //return list url
        return links
    }

//    private fun print(msg: String, vararg args: Any) {
//        println(String.format(msg, *args))
//    }

//    private fun trim(s: String, width: Int): String {
//        return if (s.length > width) s.substring(0, width - 1) + "." else s
//    }

    private fun List<String>.mediaLinks(): List<String> {
        return this.filter { str ->
            val etx: String = str.split(".").last()
            //this is full extension
//            etx == "mp4" || etx == "flv" || etx == "m4a" || etx == "3gp" || etx == "mkv" ||
//                    etx == "mp3" || etx == "ogg" || etx == "jpg" || etx == "png" || etx == "gif"
            //this is extension that is supported by connect-sdk
            etx == "mp4" || etx == "mp3" || etx == "png" || etx == "jpg"
        }
    }

    private fun List<String>.toWebMedias(): List<WebMedia> {
        return this.map { url ->
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