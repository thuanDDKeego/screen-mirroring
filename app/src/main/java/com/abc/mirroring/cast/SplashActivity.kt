package com.abc.mirroring.cast

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.applovin.sdk.AppLovinSdk
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.AndroidEntryPoint
import dev.sofi.ads.AdCenter
import kotlinx.coroutines.withTimeout
import com.abc.mirroring.cast.setup.config.AppPreferences
import timber.log.Timber
import java.util.concurrent.Executors

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    private var mCastContext: CastContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            splashScreen.setKeepOnScreenCondition { true }
        }

        super.onCreate(savedInstanceState)


        apply {
            chromecast()
            // applovin()
        }

        setContent {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    modifier = Modifier.padding(16.dp),
                    painter = painterResource(id = R.drawable.tutorial_cast_2),
                    contentDescription = "loading"
                )
                Text("Loading")

                LaunchedEffect(Unit) {
                    //return@LaunchedEffect
                    val isUserComeback = AppPreferences.appOpenCounter != null && AppPreferences.appOpenCounter!! > 0

                    AppPreferences.appOpenCounter = AppPreferences.appOpenCounter!! + 1
                    if (isUserComeback) {
                        /*
                        inside this scope means user comeback app and using app on the n th time
                         */
                        try {
                            withTimeout(5000) {
                                Timber.i("Loading Splash with App Open Ads")
                                AdCenter.getInstance().appOpen?.hardload(this@SplashActivity)
                                Timber.i("Loaded Splash with App Open Ads")

                                AdCenter.getInstance().appOpen?.show(this@SplashActivity) {
                                    Timber.i("Finished loading ads")
                                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        } catch (e: Exception) {
                            Timber.i("Loading ads cost more than 5 secs, suspend it")
                            val intent = Intent(this@SplashActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                    } else {
                        /**
                         * User open app in the first time!
                         */
                        Timber.i("First time user open app, passed ads")
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

            }
        }


//        lifecycleScope.launchWhenCreated {
//            if (isUserComeback) {
//                /*
//                inside this scope means user comeback app and using app on the n th time
//                 */
//                Timber.i("Loading Splash with App Open Ads")
//                val start = Date().time
//                try {
//                    withTimeout(5000) {
//                        AdCenter.getInstance().appOpen?.hardload(this@SplashActivity)
//                        Timber.i("Loaded Splash with App Open Ads")
//                    }
//                } catch (e: Exception) {
//                    Timber.i("Loading ads cost more than 5 secs, suspend it")
//                }
//
//                val end = Date().time
//                Timber.i("Finished loading splash screen in ${(end - start)} milliseconds")
//
//                AdCenter.getInstance().appOpen?.show(this@SplashActivity) {
//                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                }
//            } else {
//                /**
//                 * User open app in the first time!
//                 */
//                Timber.i("First time user open app, passed ads")
//                val intent = Intent(this@SplashActivity, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//        }
    }

    /**
    Come along with: [app/src/main/AndroidManifest.xml:99]

    <meta-data
    android:name="com.google.android.gms.cast.framework.OPTIONS_PROVIDER_CLASS_NAME"
    android:value="net.sofigo.cast.tv.shared.cast.chromecast.CastOptionsProvider" />

     */
    private fun chromecast() {
        CastContext.getSharedInstance(this, Executors.newSingleThreadExecutor())
            .addOnSuccessListener {
                mCastContext = it
                Timber.d("Chromecast context started")
            }
    }

    /**
     * Come along with: [app/src/main/AndroidManifest.xml:103]
    <meta-data android:name="applovin.sdk.key"
    android:value="0GFzWuEbATOdwK224DCCGi52bXKBtzTA8oLqVxGzAQ5Zu6DEGpFz9Ueva1IGuQUeaTNRi0PdIun5JtTfdbOmUw"/>
     */
    private fun applovin() {
        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.initializeSdk(this) {
            // AppLovin SDK is initialized, start loading ads
            Timber.d("Applovin started")
        }
    }
}