package com.abc.mirroring.ui.tutorial

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abc.mirroring.R
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

}