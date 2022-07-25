package com.abc.sreenmirroring.ui.browsermirror

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abc.sreenmirroring.service.ServiceMessage

class StreamViewModel: ViewModel() {
    private val serviceMessageLiveData = MutableLiveData<ServiceMessage>()

    fun getServiceMessageLiveData(): LiveData<ServiceMessage> = serviceMessageLiveData
}