package com.abc.mirroring.cast

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.connectsdk.device.ConnectableDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sofi.ads.AdCenter
import kotlinx.coroutines.launch
import com.abc.mirroring.cast.shared.cast.Caster
import timber.log.Timber
import javax.inject.Inject

/**
 * Main ViewModel
 *
 * App được code bằng kiến trúc 1-n
 * => 1 activity & n compose screens
 *
 * Activity sẽ có 1 main view model để share data chung giữa các screens -> [net.sofigo.cast.tv.screen.MainVimel]
 * Ngoài ra, từng compose screens cũng sẽ có các viewmodel riêng để giữ logic riêng
 */
@HiltViewModel
class GlobalVimel @Inject constructor(
    val caster: Caster
) : VimelStateHolder<GlobalVimel.GlobalState>(GlobalState()) {

    val ads: AdCenter = AdCenter.getInstance()

    data class GlobalState(
        var count: Int = 0,
        var isDeviceConnected: Boolean = false,
        var deviceName: String = "",
        var device: ConnectableDevice? = null
    ) : State

    /**
     * Su dung ham nay de goi MainVimel o moi noi
     */
    companion object {

        @Composable
        fun get(): GlobalVimel {
            val composeView = LocalView.current
            val storeOwner = composeView.findViewTreeViewModelStoreOwner()

            if (storeOwner != null) {
                return hiltViewModel(storeOwner)
            }
            return hiltViewModel()
        }
    }

    init {
        viewModelScope.launch {
            caster.start().also { Timber.i("Caster initialized") }
            caster.discovery.device.collect {
                update { state ->
                    state.copy(
                        isDeviceConnected = it?.isConnected == true,
                        deviceName = it?.friendlyName ?: "",
                        device = it
                    )
                }
            }
        }
    }

    fun countInc() = update { state -> state.copy(count = state.count + 1) }


    override fun onCleared() {
        caster.shutdown()
        Timber.d("caster has shutdown")
        super.onCleared()
    }
//
//
//    val mediaRoute = MediaRouter.getInstance(context)
//
//    val castState = MutableStateFlow(CastState.NO_DEVICES_AVAILABLE)
//
//    val selector: MediaRouteSelector = MediaRouteSelector.Builder()
//        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
//        .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
//        .build()
//
//    val listener = object : MediaRouter.Callback() {
//
//        override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) {
//            Timber.d("Added $route")
//            super.onRouteAdded(router, route)
//        }
//
//        override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
//            super.onRouteChanged(router, route)
//        }
//
//        override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) {
//            super.onRouteRemoved(router, route)
//        }
//
//        override fun onRouteSelected(router: MediaRouter, route: MediaRouter.RouteInfo, reason: Int) {
//            super.onRouteSelected(router, route, reason)
//        }
//    }
//
//    init {
//        try {
//            val castingContext = CastContext.getSharedInstance(context)
//            castingContext.addCastStateListener(this)
//            mediaRoute.addCallback(selector, listener, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN)
//        } catch (e: Exception) {
//            // handle me please
//        }
//    }
//
//    fun shouldShowChooserFragment(): Boolean {
//        return when (castState.value) {
//            CastState.NOT_CONNECTED -> true
//            CastState.NO_DEVICES_AVAILABLE -> true
//            CastState.CONNECTING -> false
//            CastState.CONNECTED -> false
//            else -> false
//        }
//    }
//
//    override fun onCastStateChanged(state: Int) {
//        castState.value = state
//    }
}