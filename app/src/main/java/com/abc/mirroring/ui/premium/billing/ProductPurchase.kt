package com.abc.mirroring.ui.premium.billing

data class ProductPurchase(val id: String, val title: String, val price: String, var isSub: Boolean = false, val offerToken: String = "")


