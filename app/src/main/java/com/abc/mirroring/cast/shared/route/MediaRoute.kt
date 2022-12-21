package com.abc.mirroring.cast.shared.route

sealed class MediaRoute(val route: String){
    object Image: MediaRoute("image_route")
    object Video: MediaRoute("video_route")
    object Audio: MediaRoute("audio_route")
    object Youtube: MediaRoute("youtube_route")
//    object Drive: MediaRoute("drive_route")
    object WebCast: MediaRoute("web_cast_route")
    object IPTV: MediaRoute("iptv_route")
}