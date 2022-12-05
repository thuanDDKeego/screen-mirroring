package com.abc.mirroring.cast.section

import android.content.Context
import android.net.Uri
import kotlinx.parcelize.Parcelize
import net.sofigo.cast.tv.shared.utils.FileUtils

@Parcelize
data class Audible(
    val id: Long,
    val name: String,
    val uri: Uri,
    var dateAdded: Long,
    var filePath: String,
    var size: Int,
    var duration: Int,
    var type: MediaType
) : Streamable {
    override fun id() = id.toString()

    override fun uri() = uri
    override fun url(): String = ""

    override fun mineType(ctx: Context) = FileUtils.getMimeType(ctx, uri()) ?: "video/mp4"

    override fun source() = SourceType.Internal
    override fun mediaType(): MediaType = type

    override fun name() = name
    override fun duration(): Float {
        return duration.toFloat()
    }
}
