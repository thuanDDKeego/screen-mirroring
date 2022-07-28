package com.abc.sreenmirroring.ui.browsermirror

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abc.sreenmirroring.service.ServiceMessage

class StreamViewModel : ViewModel() {
    val serviceMessageLiveData = MutableLiveData<ServiceMessage>()

    companion object {
        private var instance: StreamViewModel? = null
        fun getInstance() =
            instance ?: synchronized(StreamViewModel::class.java) {
                instance ?: StreamViewModel().also { instance = it }
            }
    }
}
