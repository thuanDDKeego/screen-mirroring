package com.abc.mirroring.cast.shared.cast

import android.app.Activity
import android.content.Context
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.device.ConnectableDeviceListener
import com.connectsdk.device.DevicePicker
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.service.DeviceService
import com.connectsdk.service.command.ServiceCommandError
import kotlinx.coroutines.flow.MutableStateFlow
import com.abc.mirroring.cast.setup.config.AppConfigRemote
import timber.log.Timber


/**
 * Class nay tim kiem cac thiet bi ho tro cast hoac stream wireless display
 *
 * Tinh trang: Đang hardcode và dirty code, sau khi demo đc tính năng sẽ hệ thống lại cho đẹp.
 *
 * @see https://connectsdk.com/en/latest/guide-and/and-discover-connect.html
 */
class DeviceDiscovery(
    val context: Context
) : ConnectableDeviceListener {

    private var manager: DiscoveryManager

    val device = MutableStateFlow<ConnectableDevice?>(null)

    /**
     * renew everytime device connected
     */
    var volumeController: SessionPlayer.VolumeController? = null

    init {
        DiscoveryManager.init(context);
        manager = DiscoveryManager.getInstance()
        manager.registerDefaultDeviceTypes(getDeviceServiceMap())
        manager.pairingLevel = DiscoveryManager.PairingLevel.ON
        manager.start()
    }

    fun getDeviceServiceMap(): HashMap<String, String> {
        val devicesList = HashMap<String, String>()
        devicesList["com.connectsdk.service.WebOSTVService"] = "com.connectsdk.discovery.provider.SSDPDiscoveryProvider"
        devicesList["com.connectsdk.service.NetcastTVService"] = "com.connectsdk.discovery.provider.SSDPDiscoveryProvider"
        devicesList["com.connectsdk.service.DLNAService"] = "com.connectsdk.discovery.provider.SSDPDiscoveryProvider"
//        devicesList["com.connectsdk.service.DIALService"] = "com.connectsdk.discovery.provider.SSDPDiscoveryProvider"
        devicesList["com.connectsdk.service.RokuService"] = "com.connectsdk.discovery.provider.SSDPDiscoveryProvider"
        devicesList["com.connectsdk.service.CastService"] = "com.connectsdk.discovery.provider.CastDiscoveryProvider"
//        devicesList["com.connectsdk.service.AirPlayService"] = "com.connectsdk.discovery.provider.ZeroconfDiscoveryProvider"
        devicesList["com.connectsdk.service.FireTVService"] = "com.connectsdk.discovery.provider.FireTVDiscoveryProvider"
        return devicesList
    }

    fun discover() {
        // This step could even happen in your app's delegate
        manager = DiscoveryManager.getInstance()
        manager.pairingLevel = DiscoveryManager.PairingLevel.ON

        Timber.i("Register Devices: ${AppConfigRemote.getDevices()}")

//        manager.setCapabilityFilters(
//            CapabilityFilter(
//                MediaPlayer.Play_Video,
//                MediaControl.Any,
//                VolumeControl.Volume_Up_Down
//            ),
//            CapabilityFilter(MediaPlayer.Play_Playlist),
//            CapabilityFilter(MediaPlayer.Play_Audio),
//            CapabilityFilter(MediaPlayer.Display_Image)
//        );

        manager.start();
    }


    fun stop() {
        manager.stop()
    }

    fun picker(context: Activity) {
        val dialog = DevicePicker(context).getPickerDialog("Devices") { adapter, view, position, id ->
            val mDevice = adapter.getItemAtPosition(position) as ConnectableDevice
            mDevice.addListener(this)
            mDevice.connect()
        }
        dialog.show()
    }

    override fun onDeviceReady(device: ConnectableDevice?) {
        Timber.d("connected to $device")
        this.device.value?.disconnect() // disconnect last session if exists
        this.device.value = device
        this.volumeController = SessionPlayer.VolumeController(device!!)
    }

    override fun onDeviceDisconnected(device: ConnectableDevice?) {
        /* update the device*/
        Timber.d("disconnected with $device")
        if (this.device.value?.id == device?.id) {
            this.device.value = null
            this.volumeController = null
        }
    }

    override fun onPairingRequired(
        device: ConnectableDevice?,
        service: DeviceService?,
        pairingType: DeviceService.PairingType?
    ) {
        Timber.d("Not yet implemented")
    }

    override fun onCapabilityUpdated(
        device: ConnectableDevice?,
        added: MutableList<String>?,
        removed: MutableList<String>?
    ) {
        Timber.d("Not yet implemented")
    }

    override fun onConnectionFailed(device: ConnectableDevice?, error: ServiceCommandError?) {
        Timber.d("Not yet implemented")
    }

}