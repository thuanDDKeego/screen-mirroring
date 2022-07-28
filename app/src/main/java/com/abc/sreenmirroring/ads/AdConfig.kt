import com.abc.sreenmirroring.R

object AdConfig {
    const val TURN_OFF_ADS = false

    const val AD_MOB_SPLASH_INTERSTITIAL = R.string.ad_interstitial_splash
    const val AD_MOB_GALLERY_INTERSTITIAL = R.string.ad_interstitial_gallery
    const val AD_MOB_EXPORT_INTERSTITIAL = R.string.ad_interstitial_export
    const val AD_MOB_SHARE_INTERSTITIAL = R.string.ad_interstitial_share
    const val AD_MOB_EFFECT_REWARDED_INTERSTITIAL = R.string.ad_rewarded_effect
    const val AD_MOB_FILTER_REWARDED_INTERSTITIAL = R.string.ad_rewarded_filter
    const val AD_MOB_MUSIC_REWARDED_INTERSTITIAL = R.string.ad_rewarded_music
    const val AD_MOB_STICKER_REWARDED_INTERSTITIAL = R.string.ad_rewarded_sticker
    const val AD_MOB_WATERMARK_REWARDED_INTERSTITIAL = R.string.ad_rewarded_watermark

    const val AD_MOB_HOME_BOTTOM_NATIVE = R.string.ad_native_home_bottom
    const val AD_MOB_GALLERY_BOTTOM_NATIVE = R.string.ad_native_gallery_bottom
    const val AD_MOB_SHARE_BOTTOM_NATIVE = R.string.ad_native_share_bottom

    const val AD_MOB_EDIT_VIDEO_BANNER = R.string.ad_edit_video_banner

    const val AD_MOB_AD_OPEN = R.string.ad_app_open
}

enum class AdType(var adsId: Int) {
    GALLERY_INTERSTITIAL(AdConfig.AD_MOB_GALLERY_INTERSTITIAL),
    EXPORT_INTERSTITIAL(AdConfig.AD_MOB_EXPORT_INTERSTITIAL),
    SPLASH_INTERSTITIAL(AdConfig.AD_MOB_SPLASH_INTERSTITIAL),
    BROWSER_MIRROR_REWARD(AdConfig.AD_MOB_EFFECT_REWARDED_INTERSTITIAL),
    FILTER_REWARDED(AdConfig.AD_MOB_FILTER_REWARDED_INTERSTITIAL),
    MUSIC_REWARDED(AdConfig.AD_MOB_MUSIC_REWARDED_INTERSTITIAL),
    STICKER_REWARDED(AdConfig.AD_MOB_STICKER_REWARDED_INTERSTITIAL),
    WATERMARK_REWARDED(AdConfig.AD_MOB_WATERMARK_REWARDED_INTERSTITIAL),

    HOME_NATIVE(AdConfig.AD_MOB_HOME_BOTTOM_NATIVE),
    GALLERY_NATIVE(AdConfig.AD_MOB_GALLERY_BOTTOM_NATIVE),
    SHARE_NATIVE(AdConfig.AD_MOB_SHARE_BOTTOM_NATIVE),

    APP_OPEN(AdConfig.AD_MOB_AD_OPEN);
}