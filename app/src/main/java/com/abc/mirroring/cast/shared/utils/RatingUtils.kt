package com.abc.mirroring.cast.shared.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import net.sofigo.cast.tv.BuildConfig

object RatingUtils {

    fun rateInStore(activity: Activity) {
//        try {
//            val manager = ReviewManagerFactory.create(activity)
//            val request = manager.requestReviewFlow()
//            request.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // We got the ReviewInfo object
//                    val reviewInfo = task.result
//                    val flow = manager.launchReviewFlow(activity, reviewInfo)
//                    flow.addOnCompleteListener { _ ->
//                        // The flow has finished. The API does not indicate whether the user
//                        // reviewed or not, or even whether the review dialog was shown. Thus, no
//                        // matter the result, we continue our app flow.
//                        Toast.makeText(activity, activity.getString(R.string.thanks_for_your_review), Toast.LENGTH_LONG).show()
//                    }
//                } else {
//                    // There was some problem, log or handle the error code.
////                    @ReviewErrorCode val reviewErrorCode = (task.getException() as Exception).errorCode
//                    openAppStore(activity)
//                }
//            }
//        }
//        catch(e: Exception) {
        openAppStore(activity)
//        }
    }

    private fun openAppStore(activity: Activity) {
        val url =
            "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        activity.startActivity(intent)
    }
}