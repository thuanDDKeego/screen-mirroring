package info.dvkr.screenstream.data.httpserver

import android.content.Context
import com.elvishew.xlog.XLog
import info.dvkr.screenstream.data.R
import info.dvkr.screenstream.data.other.getFileFromAssets
import info.dvkr.screenstream.data.other.getLog
import info.dvkr.screenstream.data.other.randomString
import info.dvkr.screenstream.data.settings.SettingsReadOnly
import java.nio.charset.StandardCharsets

class HttpServerFiles(context: Context, private val settingsReadOnly: SettingsReadOnly) {
    companion object {
        const val FAVICON_PNG = "favicon.webp"
        const val LOGO_PNG = "logo.webp"
        const val FULLSCREEN_ON_PNG = "fullscreen-on.png"
        const val FULLSCREEN_OFF_PNG = "fullscreen-off.png"
        const val START_STOP_PNG = "start-stop.png"

        private const val INDEX_HTML = "index.html"
        private const val INDEX_HTML_BACKGROUND_COLOR = "BACKGROUND_COLOR"
        private const val INDEX_HTML_SCREEN_STREAM_ADDRESS = "SCREEN_STREAM_ADDRESS"
        private const val INDEX_HTML_ENABLE_BUTTONS = "ENABLE_BUTTONS"

        private const val PINREQUEST_HTML = "pinrequest.html"
        private const val PINREQUEST_HTML_STREAM_REQUIRE_PIN = "STREAM_REQUIRE_PIN"
        private const val PINREQUEST_HTML_ENTER_PIN = "ENTER_PIN"
        private const val PINREQUEST_HTML_SUBMIT_TEXT = "SUBMIT_TEXT"
        private const val PINREQUEST_HTML_WRONG_PIN_MESSAGE = "WRONG_PIN_MESSAGE"

        private const val ADDRESS_BLOCKED_HTML = "blocked.html"
        private const val ADDRESS_BLOCKED_HTML_ADDRESS_BLOCKED = "ADDRESS_BLOCKED"

        const val ROOT_ADDRESS = "/"
        const val PIN_REQUEST_ADDRESS = "pinrequest"
        const val CLIENT_BLOCKED_ADDRESS = "blocked"
        const val START_STOP_ADDRESS = "start-stop"
        const val PIN_PARAMETER = "pin"
    }

    private val applicationContext: Context = context.applicationContext

    val faviconPng: ByteArray = applicationContext.getFileFromAssets(FAVICON_PNG)
    val logoPng: ByteArray = applicationContext.getFileFromAssets(LOGO_PNG)
    val fullscreenOnPng: ByteArray = applicationContext.getFileFromAssets(FULLSCREEN_ON_PNG)
    val fullscreenOffPng: ByteArray = applicationContext.getFileFromAssets(FULLSCREEN_OFF_PNG)
    val startStopPng: ByteArray = applicationContext.getFileFromAssets(START_STOP_PNG)

    private val baseIndexHtml =
        String(applicationContext.getFileFromAssets(INDEX_HTML), StandardCharsets.UTF_8)

    private val basePinRequestHtml =
        String(applicationContext.getFileFromAssets(PINREQUEST_HTML), StandardCharsets.UTF_8)
            .replaceFirst(
                PINREQUEST_HTML_STREAM_REQUIRE_PIN.toRegex(),
                applicationContext.getString(R.string.html_stream_require_pin)
            )
            .replaceFirst(
                PINREQUEST_HTML_ENTER_PIN.toRegex(),
                applicationContext.getString(R.string.html_enter_pin)
            )
            .replaceFirst(
                PINREQUEST_HTML_SUBMIT_TEXT.toRegex(),
                applicationContext.getString(R.string.html_submit_text)
            )

    private val baseAddressBlockedHtml: String =
        String(applicationContext.getFileFromAssets(ADDRESS_BLOCKED_HTML), StandardCharsets.UTF_8)

    var htmlEnableButtons: Boolean = settingsReadOnly.htmlEnableButtons
    var htmlBackColor: Int = settingsReadOnly.htmlBackColor
    var pin: String = settingsReadOnly.pin
    var streamAddress: String = configureStreamAddress()
    var jpegFallbackAddress: String = configureJpegFallbackAddress()
    var indexHtml: String = configureIndexHtml(streamAddress)
    var pinRequestHtml: String = configurePinRequestHtml()
    var pinRequestErrorHtml: String = configurePinRequestErrorHtml()
    var addressBlockedHtml: String = configureAddressBlockedHtml()

    private var enablePin: Boolean = settingsReadOnly.enablePin

    fun configure() {
        XLog.d(getLog("configure"))

        htmlEnableButtons = settingsReadOnly.htmlEnableButtons
        htmlBackColor = settingsReadOnly.htmlBackColor
        enablePin = settingsReadOnly.enablePin
        pin = settingsReadOnly.pin
        streamAddress = configureStreamAddress()
        jpegFallbackAddress = configureJpegFallbackAddress()
        indexHtml = configureIndexHtml(streamAddress)

        pinRequestHtml = configurePinRequestHtml()
        pinRequestErrorHtml = configurePinRequestErrorHtml()
        addressBlockedHtml = configureAddressBlockedHtml()
    }

    private fun configureStreamAddress(): String = if (enablePin) "${randomString(16)}.mjpeg" else "stream.mjpeg"

    private fun configureJpegFallbackAddress(): String = streamAddress.dropLast(5) + "jpeg"

    private fun configureIndexHtml(streamAddress: String): String =
        baseIndexHtml
            .replaceFirst(
                INDEX_HTML_ENABLE_BUTTONS.toRegex(),
                (htmlEnableButtons && enablePin.not()).toString()
            )
            .replaceFirst(
                INDEX_HTML_BACKGROUND_COLOR.toRegex(),
                "#%06X".format(0xFFFFFF and htmlBackColor)
            )
            .replace(INDEX_HTML_SCREEN_STREAM_ADDRESS.toRegex(), streamAddress)

    private fun configurePinRequestHtml(): String =
        if (enablePin)
            basePinRequestHtml.replaceFirst(PINREQUEST_HTML_WRONG_PIN_MESSAGE.toRegex(), "&nbsp")
        else
            ""

    private fun configurePinRequestErrorHtml(): String =
        if (enablePin)
            basePinRequestHtml
                .replaceFirst(
                    PINREQUEST_HTML_WRONG_PIN_MESSAGE.toRegex(),
                    applicationContext.getString(R.string.html_wrong_pin)
                )
        else
            ""

    private fun configureAddressBlockedHtml(): String =
        if (enablePin)
            baseAddressBlockedHtml.replaceFirst(
                ADDRESS_BLOCKED_HTML_ADDRESS_BLOCKED.toRegex(),
                applicationContext.getString(R.string.html_address_blocked)
            )
        else
            ""
}