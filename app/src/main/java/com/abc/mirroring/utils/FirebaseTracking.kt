package com.abc.mirroring.utils

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object FirebaseTracking {
    fun logSplashShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.SPLASH_SHOWED, params)
    }

    fun logOnBoardingShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.ONBOARDING_SHOWED, params)
    }

    fun logHomeShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.HOME_SHOWED, params)
    }

    fun logHomeMirrorClicked() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.HOME_MIRROR_CLICKED, params)
    }

    fun logHomeCardBrowserClicked() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.HOME_CARD_BROWSER_CLICKED, params)
    }

    fun logHomeBrowserDialogShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.HOME_BROWSER_DIALOG_SHOWED, params)
    }

    fun logHomeFloatingClicked(modeOn: Boolean) {
        val params = Bundle()
        params.putBoolean("modeOn", modeOn)
        Firebase.analytics.logEvent(FirebaseLogEvent.HOME_FLOATING_CLICKED, params)
    }

    fun logMirrorSelectDeviceShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.MIRROR_SELECT_DEVICE_SHOWED, params)
    }

    fun logBrowserStartShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.BROWSER_START_SHOWED, params)
    }

    fun logSettingShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.SETTING_SHOWED, params)
    }

    fun logHomeIconHelpClicked() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.HOME_ICON_HELP_CLICKED, params)
    }

    fun logHelpTutorialShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.HELP_TUTORIAL_SHOWED, params)
    }

    fun logHelpFAQShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.HELP_FAQ_SHOWED, params)
    }

    fun logHelpDevicesShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.HELP_DEVICE_SHOWED, params)
    }
}

object FirebaseLogEvent {
    const val SPLASH_SHOWED = "SplashShowed"
    const val ONBOARDING_SHOWED = "OnBoardingShowed"
    const val HOME_SHOWED = "HomeShowed"
    const val HOME_MIRROR_CLICKED = "HomeMirrorClicked"
    const val HOME_CARD_BROWSER_CLICKED = "HomeCardBrowserClicked"
    const val HOME_BROWSER_DIALOG_SHOWED = "HomeBrowserDialogShowed"
    const val HOME_FLOATING_CLICKED = "HomeFloatingClicked"
    const val MIRROR_SELECT_DEVICE_SHOWED = "MirrorSelectDeviceShowed"
    const val BROWSER_START_SHOWED = "BrowserStartShowed"
    const val SETTING_SHOWED = "SettingShowed"
    const val HOME_ICON_HELP_CLICKED = "HomeIconHelpClicked"
    const val HELP_TUTORIAL_SHOWED = "HelpTutorialShowed"
    const val HELP_FAQ_SHOWED = "HelpFAQShowed"
    const val HELP_DEVICE_SHOWED = "HelpDevicesShowed"
}