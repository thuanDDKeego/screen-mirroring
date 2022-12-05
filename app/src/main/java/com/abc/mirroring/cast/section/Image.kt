package com.abc.mirroring.cast.section

import android.content.Context
import android.net.Uri
import kotlinx.parcelize.Parcelize
import net.sofigo.cast.tv.shared.utils.FileUtils

@Parcelize
data class Image(
    val id: Long,
    val name: String,
    val uri: Uri,
    val dateAdded: Long,
) : Streamable {
    override fun id(): String {
        return id.toString()
    }

    override fun mediaType(): MediaType = MediaType.Image

    override fun uri() = uri
    override fun url(): String {
        TODO("Not yet implemented")
    }

    override fun mineType(ctx: Context) = FileUtils.getMimeType(ctx, uri()) ?: "image/jpeg"

    override fun source() = SourceType.Internal

    override fun name() = name
    override fun duration(): Float {
        TODO("Not yet implemented")
    }
}