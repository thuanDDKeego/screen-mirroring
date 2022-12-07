package com.abc.mirroring.ui.premium.billing

data class Subscription(val id: String, val title: String, val price: String, var isSub: Boolean = false, var timeSubs: Long = 0L)
