package com.abc.mirroring.ui.tutorial

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abc.mirroring.R
import com.abc.mirroring.data.model.DeviceItem
import com.abc.mirroring.data.model.FAQItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TutorialViewModel @Inject constructor() : ViewModel() {

    var currentStepTutorial = MutableLiveData(0)

    fun setCurrentStepTutorial(step: Int) {
        currentStepTutorial.value = if (step in 0..3) step else 0
    }

    fun getFAQItem(context: Context): ArrayList<FAQItem> {
        return arrayListOf(
            FAQItem(
                context.getString(R.string.faq_item_title1),
                context.getString(
                    R.string.faq_item_description1
                )
            ),
            FAQItem("",""),
            FAQItem(
                context.getString(R.string.faq_item_title2),
                context.getString(R.string.faq_item_description2)
            ),
            FAQItem(
                context.getString(R.string.faq_item_title3),
                context.getString(R.string.faq_item_description3)
            ),
            FAQItem(
                context.getString(R.string.faq_item_title4),
                context.getString(R.string.faq_item_description4)
            ),
            FAQItem(
                context.getString(R.string.faq_item_title5),
                context.getString(R.string.faq_item_description5)
            )
        )
    }

    fun getDeviceItem(context: Context): ArrayList<DeviceItem> {
        return arrayListOf(
            DeviceItem(
                context.getString(R.string.dlna),
                context.getString(R.string.dlna_content),
                R.drawable.img_dlna
            ),
            DeviceItem(
                context.getString(R.string.chrome_cast),
                context.getString(R.string.chrome_cast_content),
                R.drawable.img_chromecast
            ),
            DeviceItem(
                context.getString(R.string.xbox),
                context.getString(R.string.xbox_content),
                R.drawable.img_xbox
            ),
            DeviceItem(
                context.getString(R.string.fire_tv),
                context.getString(R.string.fire_tv_content),
                R.drawable.img_fire_tv
            ),
            DeviceItem(
                context.getString(R.string.roku),
                context.getString(R.string.roku_content),
                R.drawable.img_roku
            ),
            DeviceItem(
                context.getString(R.string.apple_tv),
                context.getString(R.string.apple_tv_content),
                R.drawable.img_apple
            ),
            DeviceItem(
                context.getString(R.string.lg_web_os),
                context.getString(R.string.lg_web_os_content),
                R.drawable.img_webos
            )
        )
    }
}