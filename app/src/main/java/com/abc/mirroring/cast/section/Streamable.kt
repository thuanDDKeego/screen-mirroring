package com.abc.mirroring.cast.section

import android.content.Context
import android.net.Uri
import android.os.Parcelable

interface Streamable : Parcelable {
    fun id(): String
    fun uri(): Uri?
    fun url(): String
    fun mineType(ctx: Context): String
    fun source(): SourceType
    fun mediaType(): MediaType

    fun name(): String
    fun duration(): Float
}

enum class SourceType {
    Internal,
    External
}