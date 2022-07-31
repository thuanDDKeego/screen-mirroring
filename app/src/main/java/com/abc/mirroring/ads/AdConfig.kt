import com.abc.mirroring.R

object AdConfig {
    const val TURN_OFF_ADS = false

    const val AD_MOB_SPLASH_INTERSTITIAL = R.string.ad_interstitial_splash
    const val AD_MOB_MIRROR_DEVICE_INTERSTITIAL = R.string.ad_interstitial_gallery
    const val AD_MOB_BACK_FROM_TUTORIAL_INTERSTITIAL = R.string.ad_interstitial_export
    const val AD_MOB_HOME_ONBOARDING_INTERSTITIAL = R.string.ad_interstitial_share
    const val AD_MOB_EFFECT_REWARDED_INTERSTITIAL = R.string.ad_rewarded_effect
    const val AD_MOB_FILTER_REWARDED_INTERSTITIAL = R.string.ad_rewarded_filter
    const val AD_MOB_MUSIC_REWARDED_INTERSTITIAL = R.string.ad_rewarded_music
    const val AD_MOB_STICKER_REWARDED_INTERSTITIAL = R.string.ad_rewarded_sticker
    const val AD_MOB_WATERMARK_REWARDED_INTERSTITIAL = R.string.ad_rewarded_watermark

    const val AD_MOB_HOME_BOTTOM_NATIVE = R.string.ad_native_home_bottom
    const val AD_MOB_EXIT_APP_NATIVE = R.string.ad_native_home_bottom
    const val AD_MOB_TUTORIAL_NATIVE = R.string.ad_native_gallery_bottom
    const val AD_MOB_FAQ_NATIVE = R.string.ad_native_gallery_bottom
    const val AD_MOB_CONNECT_DEVICE_NATIVE = R.string.ad_native_gallery_bottom
    const val AD_MOB_BROWSER_DEVICE_NATIVE = R.string.ad_native_share_bottom

    const val AD_MOB_EDIT_VIDEO_BANNER = R.string.ad_edit_video_banner

    const val AD_MOB_AD_OPEN = R.string.ad_app_open
}

enum class AdType(var adsId: Int) {
    SPLASH_INTERSTITIAL(AdConfig.AD_MOB_SPLASH_INTERSTITIAL),
    HOME_ONBOARDING_INTERSTITIAL(AdConfig.AD_MOB_HOME_ONBOARDING_INTERSTITIAL),
    BACK_FROM_TUTORIAL_INTERSTITIAL(AdConfig.AD_MOB_BACK_FROM_TUTORIAL_INTERSTITIAL),
    GO_MIRROR_DEVICE_INTERSTITIAL(AdConfig.AD_MOB_MIRROR_DEVICE_INTERSTITIAL),
    MIRROR_DEVICE_NATIVE(AdConfig.AD_MOB_BROWSER_DEVICE_NATIVE),
    BROWSER_MIRROR_REWARD(AdConfig.AD_MOB_EFFECT_REWARDED_INTERSTITIAL),
    HOME_NATIVE(AdConfig.AD_MOB_HOME_BOTTOM_NATIVE),
    EXIT_APP_NATIVE(AdConfig.AD_MOB_EXIT_APP_NATIVE),
    TUTORIAL_NATIVE(AdConfig.AD_MOB_TUTORIAL_NATIVE),
    FAQ_NATIVE(AdConfig.AD_MOB_FAQ_NATIVE),
    CONNECT_DEVICE_NATIVE(AdConfig.AD_MOB_CONNECT_DEVICE_NATIVE),

    APP_OPEN(AdConfig.AD_MOB_AD_OPEN);
}