package com.abc.mirroring.base

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.*
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.abc.mirroring.config.AppPreferences
import java.util.*

abstract class BaseActivity<V : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: V
    open var isFullScreen: Boolean = false

    companion object {
        var dLocale: Locale? = Locale(AppPreferences().languageSelected.toString())
    }

    init {
        if (dLocale != Locale("")) {
            Locale.setDefault(dLocale)
            val configuration = Configuration()
            configuration.setLocale(dLocale)
            this.applyOverrideConfiguration(configuration)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = initBinding()
        if (isFullScreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setContentView(binding.root)
        initViews()
        initActions()
        initAdmob()
    }

    fun Activity.openAppInStore() {
        try {
            val intent = packageManager.getLaunchIntentForPackage("com.android.vending")
            if (intent != null) {
                intent.action = Intent.ACTION_VIEW
                intent.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
        }
    }

    protected fun observerWifiState(onWifiChangeStateConnection: onWifiChangeStateConnection) {
        val connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (networkInfo?.detailedState == NetworkInfo.DetailedState.DISCONNECTED) {
            onWifiChangeStateConnection.onWifiUnavailable()
        }
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                onWifiChangeStateConnection.onWifiAvailable()
            }

            // lost network connection
            override fun onLost(network: Network) {
                super.onLost(network)
                onWifiChangeStateConnection.onWifiUnavailable()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val connectivityManager =
                getSystemService(ConnectivityManager::class.java) as ConnectivityManager
            connectivityManager.requestNetwork(networkRequest, networkCallback)
        }
    }

    abstract fun initBinding(): V
    abstract fun initViews()
    abstract fun initActions()
    open fun initAdmob() {}
    interface onWifiChangeStateConnection {
        fun onWifiUnavailable()
        fun onWifiAvailable()
    }
}