package com.abc.mirroring.cast.section

import android.content.Context
import android.net.Uri
import kotlinx.parcelize.Parcelize

@Parcelize
data class M3U8File(
    var url: String,
    var name: String,
    var duration: Long,
    var thumbnail: String? = null,
) : Streamable {
    override fun id(): String {
        return url
    }

    override fun uri(): Uri? = null

    override fun url() = url

    override fun mineType(ctx: Context) = "application/x-mpegurl"

    override fun source() = SourceType.External

    override fun mediaType(): MediaType {
        return MediaType.M3U8File
    }
    override fun name() = name

    override fun duration(): Float {
        return duration.toFloat()
    }
}