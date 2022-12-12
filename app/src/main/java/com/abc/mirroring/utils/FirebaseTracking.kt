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

    fun log(type: String, params: Bundle = Bundle()){
        Firebase.analytics.logEvent(type, params)
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

    const val Home_Click_Mirror_to_TV="Home_Click_Mirror-to-TV"
    const val Home_Click_Mirror_to_Web="Home_Click_Mirror-to-Web"
    const val Home_Click_Turn_Off_Floating_Tools="Home_Click_Turn_Off_Floating-Tools"
    const val Home_Click_Turn_On_Floating_Tools="Home_Click_Turn_On_Floating-Tools"
    const val Home_Click_Help="Home_Click_Help"
    const val Home_Click_Setting="Home_Click_Setting"
    const val Home_Click_Youtube="Home_Click_Youtube"
    const val Home_Click_Image="Home_Click_Image"
    const val Home_Click_Audio="Home_Click_Audio"
    const val Home_Click_Web_Cast="Home_Click_Web-Cast"
    const val Home_Click_Video="Home_Click-Video"
    const val Home_Click_Drive="Home_Click_Drive"
    const val Home_Click_Premium="Home_Click_Premium"
    const val Home_Click_Connect_Devices="Home_Click_Connect-devices"
    const val Home_Click_Gift_icon="Home_Click_Gift-icon"
    const val Home_Click_Online_Image = "Home_Click_Online-Image"
    const val Home_Click_Iptv = "Home_Click_Iptv"
    const val Home_Click_Google_Photo = "Home_Click_Google-Photo"

    const val Tutorial_Click_next_1="Tutorial_Click_next-1"
    const val Tutorial_Click_next_2="Tutorial_Click_next-2"
    const val Tutorial_Click_previous_1="Tutorial_Click_previous-1"
    const val Tutorial_Click_previous_2="Tutorial_Click_previous-2"
    const val Tutorial_Click_Cast="Tutorial_Click_Cast"
    const val Tutorial_Click_FAQ="Tutorial_Click_FAQ"
    const val FAQ_Click_Question="FAQ_Click_Question"
    const val Tutorial_Click_Connecting_devices="Tutorial_Click_Connecting-devices"
    const val Tutorial_Click_Back="Tutorial_Click_Back"
    const val Mirror_to_Tv_Click_Select_Device="Mirror-to-Tv_Click_Select Device"
    const val Mirror_to_Tv_Click_Back="Mirror-to-Tv_Click_Back"
    const val Browser_Mirror_Popup_Click_Close="Browser-Mirror-Popup_Click_Close"
    const val Browser_Mirror_Popup_Click_Video_Starting="Browser-Mirror-Popup_Click_Video-Starting"
    const val Browser_Mirror_Click_Copy="Browser-Mirror_Click_Copy"
    const val Browser_Mirror_Click_Stop_Stream="Browser-Mirror_Click_Stop-Stream"
    const val Browser_Mirror_Click_Start_Stream="Browser-Mirror_Click_Start-Stream"
    const val Floating_Tool_Click_itself="Floating-Tool_Click_itself"
    const val Floating_Tool_Click_Timing="Floating-Tool_Click_Timing"
    const val Floating_Tool_Click_Camera="Floating-Tool_Click_Camera"
    const val Floating_Tool_Click_Paint="Floating-Tool_Click_Paint"
    const val Floating_Tool_Click_Home="Floating-Tool_Click_Home"
    const val Youtube_Click_Home="Youtube_Click_Home"
    const val Youtube_Click_Quality_Option="Youtube_Click_Quality Option"
    const val Youtube_Click_Back="Youtube_Click_Back"
    const val Quality_Option_Click_360_Video="Quality-Option_Click_360-video"
    const val Quality_Option_Click_720_Video="Quality-Option_Click_720-video"
    const val Video_Click_Volume_Down="Video_Click_Volume_Down"
    const val Video_Click_Volume_Up="Video_Click_Volume_Up"
    const val Video_Click_Volume_Mute="Video_Click_Volume_Mute"
    const val Video_Click_Play ="Video_Click_Play "
    const val Video_Click_Next="Video_Click_Next"
    const val Video_Click_Previous="Video_Click_Previous"
    const val Video_Click_Playlist="Video_Click_Playlist"
    const val Image_Click_Play="Image_Click_Play"
    const val Image_Click_Next="Image_Click_Next"
    const val Image_Click_Previous="Image_Click_Previous"
    const val Image_Picker_Click_Back="Image_Picker_Click_Back"
    const val Image_Player_Click_Back="Image_Player_Click_Back"
//    const val Audio_Click_Volume_Down="Audio_Click_Volume_Down"
//    const val Audio_Click_Volume_Up="Audio_Click_Volume_Up"
//    const val Audio_Click_Stop_Volume="Audio_Click_Stop-Volume"
//    const val Audio_Click_Next="Audio_Click_Next"
//    const val Audio_click_Play="Audio_click_Play"
//    const val Audio_click_Previous ="Audio_click_Previous "
//    const val Audio_Click_Playlist="Audio_Click_Playlist"
//    const val Audio_Click_Rewind="Audio_Click_Rewind"
//    const val Audio_Click_Back="Audio-Click_Back"
    const val Setting_Click_Help="Setting_Click_Help"
    const val Setting_Click_Pin_Switch="Setting_Click_Pin_Switch"
    const val Setting_Click_Change_Pin_Code="Setting_Click_Change-Pin-Code"
    const val Setting_Click_Rate="Setting_Click_Rate"
    const val Setting_Click_Language="Setting_Click_Language"
    const val Setting_Click_Feedback="Setting_Click_Feedback"
    const val Setting_Click_Invite_Friends="Setting_Click_Invite-Friends"
    const val Setting_Click_Subscription="Setting_Click_Subscription"
    const val Setting_Click_Xmas_Banner="Setting_Click_Xmas-Banner"
    const val Setting_Click_Policy="Setting_Click_Policy"
//    const val Setting_Click_Version="Setting_Click_Version"
    const val Rating_Click_1_Star="Rating-Click_1-Star"
    const val Rating_Click_2_Star="Rating-Click_2-Star"
    const val Rating_Click_3_Star="Rating-Click_3-Star"
    const val Rating_Click_4_Star="Rating-Click_4-Star"
    const val Rating_Click_5_Star="Rating-Click_5-Star"
    const val Rating_Click_Dont_Ask_Again="Rating-Click-Don\'t-ask-again"
    const val Rating_Click_Cancel = "Rating_Click_Cancel"

    const val Premium_Click_Back = "Premium_Click_Back"
    const val Premium_Click_Monthly = "Premium_Click_Monthly"
    const val Premium_Click_Life_Time = "Premium_Click_Life-time"
    const val Premium_Click_Trial = "Premium_Click_Trial"
    const val Premium_Click_Cancel = "Premium_Click_Cancel"
}