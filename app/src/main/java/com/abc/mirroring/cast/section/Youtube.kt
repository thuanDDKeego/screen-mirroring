package com.abc.mirroring.cast.section

import android.content.Context
import android.net.Uri
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.section.Streamable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Youtube(
    var url: String,
    var name: String,
    var duration: Long,
    var thumbnail: String? = null,
    var format: Int
) : Streamable {
    override fun id(): String {
        return url
    }

    override fun uri(): Uri? = null

    override fun url() = url

    override fun mineType(ctx: Context) = "video/mp4"

    override fun source() = SourceType.External
    override fun mediaType(): MediaType {
        return MediaType.Youtube
    }

    override fun name() = name
    override fun duration(): Float {
        return duration.toFloat();
    }
}