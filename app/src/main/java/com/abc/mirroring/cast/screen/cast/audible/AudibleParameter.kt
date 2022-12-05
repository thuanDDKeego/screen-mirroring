package com.abc.mirroring.cast.screen.cast.audible

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.section.Streamable

@Parcelize
data class AudibleParameter(
    val type: MediaType = MediaType.Video,
    val source: SourceType = SourceType.Internal,
    /**
     * string must be valid url, we will update this later
     * using this params if the [source] is [SourceType.External]
     */
    var urls: List<Streamable>? = null, // string must be valid url, we will update this later,
    /**
     * instance of the current play media
     */
    val current: Streamable? = null
) : Parcelable