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

//    fun logHomeMirrorClicked() {
//        val params = Bundle()
//        Firebase.analytics.logEvent(FirebaseLogEvent.HOME_MIRROR_CLICKED, params)
//    }

//    fun logHomeCardBrowserClicked() {
//        val params = Bundle()
//        Firebase.analytics.logEvent(FirebaseLogEvent.HOME_CARD_BROWSER_CLICKED, params)
//    }

    fun logHomeBrowserDialogShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.HOME_BROWSER_DIALOG_SHOWED, params)
    }

//    fun logHomeFloatingClicked(modeOn: Boolean) {
//        val params = Bundle()
//        params.putBoolean("modeOn", modeOn)
//        Firebase.analytics.logEvent(FirebaseLogEvent.HOME_FLOATING_CLICKED, params)
//    }

    fun logMirrorSelectDeviceShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.MIRROR_SELECT_DEVICE_SHOWED, params)
    }

    fun logBrowserStartShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.BROWSER_MIRROR_SHOWED, params)
    }

    fun logSettingShowed() {
        val params = Bundle()
        Firebase.analytics.logEvent(FirebaseLogEvent.SETTING_SHOWED, params)
    }

//    fun logHomeIconHelpClicked() {
//        val params = Bundle()
//        Firebase.analytics.logEvent(FirebaseLogEvent.HOME_ICON_HELP_CLICKED, params)
//    }

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
    const val SPLASH_SHOWED = "Splash_Showed"
    const val ONBOARDING_SHOWED = "OnBoarding_Showed"
    const val HOME_SHOWED = "Home_Showed"
//    const val HOME_MIRROR_CLICKED = "HomeMirrorClicked"
//    const val HOME_CARD_BROWSER_CLICKED = "HomeCardBrowserClicked"
    const val HOME_BROWSER_DIALOG_SHOWED = "HomeBrowser_Dialog_Showed"
//    const val HOME_FLOATING_CLICKED = "HomeFloatingClicked"
    const val MIRROR_SELECT_DEVICE_SHOWED = "MirrorSelectDevice_Showed"
    const val BROWSER_MIRROR_SHOWED = "BrowserMirror_Showed"
    const val SETTING_SHOWED = "Setting_Showed"
//    const val HOME_ICON_HELP_CLICKED = "HomeIconHelpClicked"
    const val HELP_TUTORIAL_SHOWED = "HelpTutorial_Showed"
    const val HELP_CAST_SHOWED = "HelpCast_Showed"
    const val HELP_FAQ_SHOWED = "HelpFAQ_Showed"
    const val HELP_DEVICE_SHOWED = "HelpDevices_Showed"

    const val Home_Click_Mirror_to_TV="Home_Click_MirrorToTV"
    const val Home_Click_Mirror_to_Web="Home_Click_MirrorToWeb"
    const val Home_Click_Turn_Off_Floating_Tools="Home_Click_Turn_Off_FloatingTools"
    const val Home_Click_Turn_On_Floating_Tools="Home_Click_Turn_On_FloatingTools"
    const val Home_Click_Help="Home_Click_Help"
    const val Home_Click_Setting="Home_Click_Setting"
    const val Home_Click_Youtube="Home_Click_Youtube"
    const val Home_Click_Image="Home_Click_Image"
    const val Home_Click_Audio="Home_Click_Audio"
    const val Home_Click_Web_Cast="Home_Click_WebCast"
    const val Home_Click_Video="Home_ClickVideo"
    const val Home_Click_Drive="Home_Click_Drive"
    const val Home_Click_Premium="Home_Click_Premium"
    const val Home_Click_Connect_Devices="Home_Click_ConnectDevices"
    const val Home_Click_Gift_icon="Home_Click_GiftIcon"
    const val Home_Click_Online_Image = "Home_Click_OnlineImage"
    const val Home_Click_Iptv = "Home_Click_Iptv"
    const val Home_Click_Google_Photo = "Home_Click_GooglePhoto"

    const val MirrorSelectDevice_Click_Tutorial = "MirrorSelectDevice_Click_Tutorial"
    const val BrowserMirror_Click_Tutorial = "BrowserMirror_Click_Tutorial"

    const val Tutorial_Click_next_1="Tutorial_Click_next1"
    const val Tutorial_Click_next_2="Tutorial_Click_next2"
    const val Tutorial_Click_previous_1="Tutorial_Click_previous1"
    const val Tutorial_Click_previous_2="Tutorial_Click_previous2"
    const val Tutorial_Click_Cast="Tutorial_Click_Cast"
    const val Tutorial_Click_FAQ="Tutorial_Click_FAQ"
    const val FAQ_Click_Question="FAQ_Click_Question"
    const val Tutorial_Click_Connecting_devices="Tutorial_Click_ConnectingDevices"
    const val Tutorial_Click_Back="Tutorial_Click_Back"
    const val Mirror_to_Tv_Click_Select_Device="MirrorToTv_Click_Select Device"
    const val Mirror_to_Tv_Click_Back="MirrorToTv_Click_Back"
    const val Browser_Mirror_Popup_Click_Close="BrowserMirrorPopup_Click_Close"
    const val Browser_Mirror_Popup_Click_Video_Starting="BrowserMirrorPopup_Click_VideoStarting"
    const val Browser_Mirror_Click_Copy="BrowserMirror_Click_Copy"
    const val Browser_Mirror_Click_Stop_Stream="BrowserMirror_Click_StopStream"
    const val Browser_Mirror_Click_Start_Stream="BrowserMirror_Click_StartStream"
    const val Floating_Tool_Click_itself="FloatingTool_Click_itself"
    const val Floating_Tool_Click_Timing="FloatingTool_Click_Timing"
    const val Floating_Tool_Click_Camera="FloatingTool_Click_Camera"
    const val Floating_Tool_Click_Paint="FloatingTool_Click_Paint"
    const val Floating_Tool_Click_Home="FloatingTool_Click_Home"
    const val Youtube_Click_Home="Youtube_Click_Home"
    const val Youtube_Click_Quality_Option="Youtube_Click_Quality Option"
    const val Youtube_Click_Back="Youtube_Click_Back"
    const val Quality_Option_Click_360_Video="QualityOption_Click_360video"
    const val Quality_Option_Click_720_Video="QualityOption_Click_720video"
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
//    const val Audio_Click_Stop_Volume="Audio_Click_StopVolume"
//    const val Audio_Click_Next="Audio_Click_Next"
//    const val Audio_click_Play="Audio_click_Play"
//    const val Audio_click_Previous ="Audio_click_Previous "
//    const val Audio_Click_Playlist="Audio_Click_Playlist"
//    const val Audio_Click_Rewind="Audio_Click_Rewind"
//    const val Audio_Click_Back="AudioClick_Back"
    const val Setting_Click_Help="Setting_Click_Help"
    const val Setting_Click_Pin_Switch="Setting_Click_Pin_Switch"
    const val Setting_Click_Change_Pin_Code="Setting_Click_ChangePinCode"
    const val Setting_Click_Rate="Setting_Click_Rate"
    const val Setting_Click_Language="Setting_Click_Language"
    const val Setting_Click_Feedback="Setting_Click_Feedback"
    const val Setting_Click_Invite_Friends="Setting_Click_InviteFriends"
    const val Setting_Click_Subscription="Setting_Click_Subscription"
    const val Setting_Click_Xmas_Banner="Setting_Click_XmasBanner"
    const val Setting_Click_Policy="Setting_Click_Policy"
//    const val Setting_Click_Version="Setting_Click_Version"
    const val Rating_Click_1_Star="RatingClick_1Star"
    const val Rating_Click_2_Star="RatingClick_2Star"
    const val Rating_Click_3_Star="RatingClick_3Star"
    const val Rating_Click_4_Star="RatingClick_4Star"
    const val Rating_Click_5_Star="RatingClick_5Star"
    const val Rating_Click_Dont_Ask_Again="RatingClick_Don\'tAskAgain"
    const val Rating_Click_Cancel = "Rating_Click_Cancel"

    const val Premium_Click_Back = "Premium_Click_Back"
    const val Premium_Click_Monthly = "Premium_Click_Monthly"
    const val Premium_Click_Life_Time = "Premium_Click_Lifetime"
    const val Premium_Click_Trial = "Premium_Click_Trial"
    const val Premium_Click_Cancel = "Premium_Click_Cancel"

    const val SmallTopBar_Click_Tutorial = "SmallTopBar_Click_Tutorial"
}