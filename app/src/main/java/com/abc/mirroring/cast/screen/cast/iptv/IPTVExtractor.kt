package com.abc.mirroring.cast.screen.cast.iptv

import com.abc.mirroring.cast.section.M3U8File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.bjoernpetersen.m3u.M3uParser
import net.bjoernpetersen.m3u.model.M3uEntry
import java.net.HttpURLConnection
import java.net.URL

object IPTVExtractor {
    fun getChannels(m3uUrl: String, onError: ((Exception) -> Unit)? = null,onSuccess: (List<M3U8File>) -> Unit) {
        val url = URL(m3uUrl)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                (url.openConnection() as HttpURLConnection).run {
//            println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")
                    inputStream.reader().use {
                        val m3u8Entries: List<M3uEntry> = M3uParser.parse(it)
                        onSuccess.invoke(m3u8Entries.mapIndexed { index, entry ->
                            M3U8File(entry.location.url.toString(), entry.title ?: "channel($index)", 0L, thumbnail = entry.metadata.logo)
                        })
                    }
                }
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
//        val url = URL("https://iptv-org.github.io/iptv/index.m3u")
//        val m3uFile = Paths.get(url.toURI().path)
//        val m3uFile = Paths.get(URL("https://iptv-org.github.io/iptv/index.m3u").toURI())
//        val fileEntries: List<M3uEntry> = M3uParser.parse(m3uFile)

//        val m3uStream: InputStream = context.resources.openRawResource(R.raw.index)
////        val m3uStream: InputStream = URL("https://iptv-org.github.io/iptv/index.m3u").openStream()
//        val m3uReader: InputStreamReader = m3uStream.reader()
//        val streamEntries: List<M3uEntry> = M3uParser.parse(m3uReader)
//        return fileEntries.mapIndexed { index, entry ->
//            M3U8File(entry.location.url.toString(), entry.title ?: "channel($index)", 0L, thumbnail = entry.metadata.logo)
//        }
    }
}