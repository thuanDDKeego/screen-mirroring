package com.abc.mirroring.cast.shared.utils

import android.content.Context
import com.abc.mirroring.R

object PremiumUtils {
    fun getBenefits(context: Context) = context.run {
        listOf(
            Benefit(R.drawable.ic_crown, 0xfffe6969,getString(R.string.unlimited_screen_mirroring)),
            Benefit(R.drawable.ic_crown, 0xff32d74b,getString(R.string.completely_ads_free)),
            Benefit(R.drawable.ic_crown, 0xff32d74b,getString(R.string.unlimited_cast_your_media_file)),
            Benefit(R.drawable.ic_crown, 0xfffdda44,getString(R.string.improved_streaming_quality))
        )
    }
    data class Benefit(val icon: Int, val tintColor: Long,val label: String)
}