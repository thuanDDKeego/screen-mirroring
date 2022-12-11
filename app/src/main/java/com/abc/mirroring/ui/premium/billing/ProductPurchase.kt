package com.abc.mirroring.ui.premium.billing


data class ProductPurchase(val id: String, val title: String, val price: String, val basePlanId: String = "", val offerTags: List<String> = listOf(), val offerToken: String = "")


