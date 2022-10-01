import com.abc.mirroring.R

object AdConfig {
    const val TURN_OFF_ADS = false

    const val AD_MOB_SPLASH_INTERSTITIAL = R.string.ad_interstitial_splash
    const val AD_MOB_MIRROR_DEVICE_INTERSTITIAL = R.string.ad_interstitial_mirror_deivce
    const val AD_MOB_BACK_FROM_TUTORIAL_INTERSTITIAL = R.string.ad_interstitial_back_from_tutorial
    const val AD_MOB_HOME_ONBOARDING_INTERSTITIAL = R.string.ad_interstitial_home_onboarding
    const val AD_MOB_BROWSER_MIRROR_REWARDED = R.string.ad_rewarded_browser_mirror

    const val AD_MOB_HOME_NATIVE = R.string.ad_native_home
    const val AD_MOB_EXIT_APP_NATIVE = R.string.ad_native_exit_app
    const val AD_MOB_TUTORIAL_NATIVE = R.string.ad_native_tutorial
    const val AD_MOB_FAQ_NATIVE = R.string.ad_native_faq
    const val AD_MOB_CONNECT_DEVICE_NATIVE = R.string.ad_native_connect_device
    const val AD_MOB_MIRROR_DEVICE_NATIVE = R.string.ad_native_mirror_device

    const val AD_MOB_AD_OPEN = R.string.ad_app_open
}

enum class AdType(var adsId: Int) {

    SPLASH_INTERSTITIAL(AdConfig.AD_MOB_SPLASH_INTERSTITIAL),
    HOME_ONBOARDING_INTERSTITIAL(AdConfig.AD_MOB_HOME_ONBOARDING_INTERSTITIAL),
    BACK_FROM_TUTORIAL_INTERSTITIAL(AdConfig.AD_MOB_BACK_FROM_TUTORIAL_INTERSTITIAL),
    GO_MIRROR_DEVICE_INTERSTITIAL(AdConfig.AD_MOB_MIRROR_DEVICE_INTERSTITIAL),
    BROWSER_MIRROR_REWARD(AdConfig.AD_MOB_BROWSER_MIRROR_REWARDED),
    HOME_NATIVE(AdConfig.AD_MOB_HOME_NATIVE),
    MIRROR_DEVICE_NATIVE(AdConfig.AD_MOB_MIRROR_DEVICE_NATIVE),
    EXIT_APP_NATIVE(AdConfig.AD_MOB_EXIT_APP_NATIVE),
    TUTORIAL_NATIVE(AdConfig.AD_MOB_TUTORIAL_NATIVE),
    FAQ_NATIVE(AdConfig.AD_MOB_FAQ_NATIVE),
    CONNECT_DEVICE_NATIVE(AdConfig.AD_MOB_CONNECT_DEVICE_NATIVE),
    APP_OPEN(AdConfig.AD_MOB_AD_OPEN);
}
