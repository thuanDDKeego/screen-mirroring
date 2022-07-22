package com.abc.sreenmirroring.ui.tutorial

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TutorialViewModel @Inject constructor(): ViewModel()  {
    var currentStepTutorial = MutableLiveData(0)

    fun setCurrentStepTutorial(step: Int) {
        currentStepTutorial.value = if (step in 0 .. 3) step else 0
    }
}