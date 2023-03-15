package com.abc.mirroring.setup

import android.app.Activity
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.setup.center.HakiObservable

object BannerObservable: HakiObservable {

    override fun onActivityPaused(activity: Activity) {
        if(activity is BaseActivity<*>){
            val adView = activity.getBannerAdView() ?: return
            adView.pause()
        }
        super.onActivityPaused(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        if(activity is BaseActivity<*>){
            val adView = activity.getBannerAdView() ?: return
            adView.resume()
        }
        super.onActivityResumed(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        if(activity is BaseActivity<*>){
            val adView = activity.getBannerAdView() ?: return
            adView.destroy()
        }
        super.onActivityDestroyed(activity)
    }
}