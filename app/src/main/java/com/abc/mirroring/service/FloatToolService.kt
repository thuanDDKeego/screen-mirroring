package com.abc.mirroring.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.MutableLiveData
import androidx.viewbinding.ViewBinding
import com.abc.mirroring.R
import com.abc.mirroring.databinding.*
import com.abc.mirroring.floatingbubble.ExpandableDrawingToolView
import com.abc.mirroring.floatingbubble.ExpandableMenuView
import com.abc.mirroring.floatingbubble.ExpandableTimerNotification
import com.abc.mirroring.floatingbubble.FloatingBubble
import com.abc.mirroring.helper.*
import com.abc.mirroring.ui.home.HomeActivity
import com.abc.mirroring.utils.NotificationUtils
import com.soft.slideshow.ads.AppOpenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


open class FloatToolService : Service() {

    companion object {
        var isRunning: Boolean = false
        var channelId = "bubble_service"
        var channelName = "floating bubble"
        var notificationId = 101
        lateinit var ctx: Context
        private var colorState = MutableLiveData(R.color.draw_black)

        fun start(context: Context) {
            ctx = context
            when {
                isRunning -> {
                    stop(context)
                }
            }
            val intentFloatToolService = getAppServiceIntent(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(context, intentFloatToolService)
            } else {
                startService(context, intentFloatToolService)
            }
        }

        fun stop(context: Context) {
            val intentFloatToolService = getAppServiceIntent(context)
            context.stopService(intentFloatToolService)
        }

        private fun getAppServiceIntent(context: Context): Intent =
            Intent(context.applicationContext, FloatToolService::class.java)

        private fun startService(context: Context, intent: Intent) = context.startService(intent)

        private fun startForeground(context: Context, intent: Intent) =
            ContextCompat.startForegroundService(context, intent)
    }

    internal class LocalBinder : Binder()

    private lateinit var context: Context
    private var height = 0
    private var width = 0
    private var xBubble = 0
    private var yBubble = 100

    private var currentPointBubble = Point(0, 0)
    private var screenSize = Size(0, 0)
    private var startX = 0
    private var screenHalfWidth = 0
    private var screenHalfHeight = 0

    private var floatingBubble: FloatingBubble? = null

    private var expandableMenuView: ExpandableMenuView? = null
    private var drawingToolView: ExpandableDrawingToolView? = null
    private var timerNotiView: ExpandableTimerNotification? = null

    private val mBinder: IBinder = LocalBinder()

    private var mNotificationManager: NotificationManager? = null
    private var mNotificationBuilder: NotificationCompat.Builder? = null


    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        context = this
        isRunning = true
        screenSize = ScreenInfo.getScreenSize(this)
        startX = screenHalfWidth - (36.toPx / 2)
        xBubble = startX
        yBubble = 0

        screenHalfWidth = screenSize.width / 2
        screenHalfHeight = screenSize.height / 2
        width = screenSize.width
        height = screenSize.height
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val intentAction = IntentAction.fromIntent(intent) //TODO
        Timber.d("onStartCommand IntentAction:...(todo)")
        if (isDrawOverlaysPermissionGranted()) {
            setupViewAppearance()
            if (isHigherThanAndroid8()) {
                startBubbleForeground()
            }
        } else throw PermissionDeniedException()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onDestroy() {
        Timber.d("onDestroy begin")
        tryRemoveAllView()
        isRunning = false
        super.onDestroy()
        Timber.d("onDestroy end")
    }

    private fun setupNotificationBuilder(
        channelId: String,
    ): Notification {
        val title = getString(R.string.app_name) //todo
        val message: String = getString(R.string.app_name) //todo

        mNotificationBuilder?.setContentTitle(title)
            ?.setContentText(message)
            ?.setOngoing(true)
            ?.setSmallIcon(R.mipmap.ic_launcher)
            ?.setContentTitle(title)
            ?.setContentText(message)
            ?.setPriority(NotificationCompat.PRIORITY_MIN)
            ?.setCategory(Notification.CATEGORY_SERVICE)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            channel.setShowBadge(false)
            channel.importance = NotificationManager.IMPORTANCE_LOW
            mNotificationManager?.createNotificationChannel(channel)
        }

        return mNotificationBuilder?.build()!!
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    private fun startBubbleForeground() {
        val channelId = if (isHigherThanAndroid8()) {
            createNotificationChannel(channelId, channelName)
        } else {
            // In earlier version, channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            ""
        }
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = setupNotificationBuilder(channelId)
        startForeground(notificationId, notification)
    }

    // public func ---------------------------------------------------------------------------------
    private fun setupViewAppearance() {

        floatingBubble = setupBubble(customFloatingBubbleAction)
            .addFloatingBubbleTouchListener(customFloatingBubbleTouchEvent)
            .build()
//        expandableView = setupExpandableView(customExpandableViewListener)
//            .build()
        onMainThread {
            tryShowBubbles()
        }
    }

    private fun setupBubble(action: FloatingBubble.Action): FloatingBubble.Builder {
        startX = (screenSize.width / 2) - (36.toPx / 2)
        xBubble = startX
        yBubble = (screenSize.height / 2)
        return FloatingBubble.Builder()
            .with(this.applicationContext)
            .setIcon(R.drawable.ic_float_bubble)
            .setRemoveIcon(R.drawable.ic_float_remove_icon)
            .addFloatingBubbleTouchListener(object : FloatingBubble.TouchEvent {
                override fun onDestroy() {
                    Timber.d("on Destroy")
                }

                override fun onClick() {
                    Timber.d("onClick")
                    updateExpandMenuView()
                    action.navigateToExpandableView()
                }

                override fun onMove(x: Int, y: Int) {
                    Timber.d("onMove X $x Y $y")

                    xBubble = x
                    yBubble =
                        if (y > height / 2) height / 2 else if (y < -height / 2) -height / 2 else y
                    currentPointBubble = Point(x, y)
                    Log.i("offset bubble: ", "height: $height width: $width  x: $x y: $y")
                }

                override fun onUp(x: Int, y: Int) {
                    Timber.d("onUp X $x Y $y")
//                    xBubble = x
//                    yBubble = y
//                    currentPointBubble = Point(x, y)
                }

                override fun onDown(x: Int, y: Int) {
                    Timber.d("onDown X $x Y $y")
                }
            })
            .setBubbleSizeDp(36)
            .setStartPoint(startX, 0)
            .setAlpha(1f)
    }


    fun updateExpandMenuView() {
        expandableMenuView = setupExpandableMenuView(customExpandableMenuViewListener)
            .build()
    }

    private fun setupExpandableMenuView(action: ExpandableMenuView.Action): ExpandableMenuView.BuilderMenu {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        Timber.d("==== ${currentPointBubble.y} === ${-screenHalfHeight + 100.toPx} ${screenHalfHeight - 100.toPx}")
        val guidelineMargin: Int =
            if (currentPointBubble.y > screenHalfHeight - 100.toPx) {
                screenHalfHeight * 2 - 150.toPx
            } else if (currentPointBubble.y < -screenHalfHeight + 100.toPx) {
                60.toPx
            } else {
                screenHalfHeight + currentPointBubble.y - (36.toPx)
            }

        Timber.d("==== guide margin ${guidelineMargin}")

        val context = applicationContext
        val binding = if (xBubble > 0) {
            FloatExpandableMenuRightBinding.inflate(inflater).apply {
                btnBubble.setOnClickListener { action.popToBubble() }
                bgScreenBubble.setOnClickListener { action.popToBubble() }
                guidelinePosition.setGuidelineBegin(guidelineMargin)
                btnTime.setOnClickListener { action.navigateToTimerNoti() }
                btnPencil.setOnClickListener { action.navigateToDrawingToolView() }
                btnCamera.setOnClickListener { action.onCameraPreview() }
                btnHome.setOnClickListener {
                    if (AppOpenManager.instance?.currentIsHomeActivity() == false) {
                        val intent = Intent(this@FloatToolService, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        AppOpenManager.instance?.currentActivity?.finish()
                    }
                    action.popToBubble()
                }
            }
        } else {
            FloatExpandableMenuLeftBinding.inflate(inflater).apply {
                btnBubble.setOnClickListener { action.popToBubble() }
                bgScreenBubble.setOnClickListener { action.popToBubble() }
                guidelinePosition.setGuidelineBegin(guidelineMargin)
                btnTime.setOnClickListener { action.navigateToTimerNoti() }
                btnPencil.setOnClickListener { action.navigateToDrawingToolView() }
                btnCamera.setOnClickListener { action.onCameraPreview() }
                btnHome.setOnClickListener {
                    if (AppOpenManager.instance?.currentIsHomeActivity() == false) {
                        val intent = Intent(this@FloatToolService, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        AppOpenManager.instance?.currentActivity?.finish()
                    }
                    action.popToBubble()
                }
            }
        }

//

        Timber.d("Bubble point X ${currentPointBubble.x} Y: ${currentPointBubble.y} ")
        Timber.d("Bubble point guidelineMargin $guidelineMargin ")

        return ExpandableMenuView.BuilderMenu()
            .with(this)
            .setExpandableView(binding.root)
            .setDimAmount(0.3f)
            .addExpandableViewListener(object : ExpandableMenuView.Action {
                override fun popToBubble() {
                    this.popToBubble()
                }

                override fun onOpenExpandableView() {
                    Timber.d("onOpenExpandableView")
                }

                override fun onCloseExpandableView() {
                    Timber.d("onCloseExpandableView")
                }
            })
    }


    fun updateDrawingToolView() {
        drawingToolView = setupDrawingToolView(customExpandableDrawingToolViewListener)
            .build()
    }

    fun updateTimerNotiView() {
        timerNotiView = setupTimerNotiView(customExpandableTimerNotiViewListener)
            .build()
    }

    private fun setupTimerNotiView(action: ExpandableTimerNotification.Action): ExpandableTimerNotification.BuilderTimerNoti {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val guidelineMargin: Int = when {
            xBubble == 0 -> screenHalfHeight - (36.toPx + 12.toPx)
            xBubble > 0 -> screenHalfHeight + currentPointBubble.y - (36.toPx)
            else -> screenHalfHeight + currentPointBubble.y - (36.toPx)
        }

        val binding = if (xBubble > 0) {
            FloatExpandableTimerRightBinding.inflate(inflater).apply {
                guidelinePosition.setGuidelineBegin(guidelineMargin)
                btnTimerBubble.setOnClickListener { action.backToBubble() }
                bgScreenBubble.setOnClickListener {
                    action.backToBubble()
                }
                txt2Second.setOnClickListener {
                    setTimerNotification(2, action, this, false)
                }
                txt5Second.setOnClickListener {
                    setTimerNotification(5, action, this, false)

                }
                txt10Second.setOnClickListener {
                    setTimerNotification(10, action, this, false)

                }
                txt15Second.setOnClickListener {
                    setTimerNotification(15, action, this, false)

                }
                txt20Second.setOnClickListener {
                    setTimerNotification(20, action, this, false)

                }
                txt30Second.setOnClickListener {
                    setTimerNotification(30, action, this, false)
                }

            }
        } else {
            FloatExpandableTimerLeftBinding.inflate(inflater).apply {
                guidelinePosition.setGuidelineBegin(guidelineMargin)
                btnTimerBubble.setOnClickListener { action.backToBubble() }
                bgScreenBubble.setOnClickListener {
                    action.backToBubble()
                }
                txt2Second.setOnClickListener {
                    setTimerNotification(2, action, this, true)
                }
                txt5Second.setOnClickListener {
                    setTimerNotification(5, action, this, true)

                }
                txt10Second.setOnClickListener {
                    setTimerNotification(10, action, this, true)

                }
                txt15Second.setOnClickListener {
                    setTimerNotification(15, action, this, true)

                }
                txt20Second.setOnClickListener {
                    setTimerNotification(20, action, this, true)

                }
                txt30Second.setOnClickListener {
                    setTimerNotification(30, action, this, true)
                }

            }
        }
        return ExpandableTimerNotification.BuilderTimerNoti()
            .with(this)
            .setDrawingToolView(binding)
            .addDrawingToolViewListener(action)
    }

    private fun setupDrawingToolView(action: ExpandableDrawingToolView.Action): ExpandableDrawingToolView.BuilderDrawingTool {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = FloatDrawingToolBinding.inflate(inflater)
        return ExpandableDrawingToolView.BuilderDrawingTool()
            .with(this)
            .setColorSate(colorState)
            .setDrawingToolView(binding)
            .addDrawingToolViewListener(action)

    }

    private fun setTimerNotification(
        mimutes: Int,
        action: ExpandableTimerNotification.Action,
        binding: ViewBinding,
        isLeft: Boolean
    ) {
        val mNotificationTime =
            System.currentTimeMillis() + mimutes * 60 * 1000 //Set after 5 seconds from the current time.
        NotificationUtils().setNotification(mNotificationTime, this@FloatToolService)
        if (isLeft) {
            binding as FloatExpandableTimerLeftBinding
            binding.txtNotiTimer.visibility = View.VISIBLE
            binding.btnTimerBubble.visibility = View.GONE
            binding.txt2Second.visibility = View.GONE
            binding.txt5Second.visibility = View.GONE
            binding.txt10Second.visibility = View.GONE
            binding.txt15Second.visibility = View.GONE
            binding.txt20Second.visibility = View.GONE
            binding.txt30Second.visibility = View.GONE
            binding.txtNotiTimer.text = getString(R.string.time_noti, mimutes.toString())
        } else {
            binding as FloatExpandableTimerRightBinding
            binding.txtNotiTimer.visibility = View.VISIBLE
            binding.btnTimerBubble.visibility = View.GONE
            binding.txt2Second.visibility = View.GONE
            binding.txt5Second.visibility = View.GONE
            binding.txt10Second.visibility = View.GONE
            binding.txt15Second.visibility = View.GONE
            binding.txt20Second.visibility = View.GONE
            binding.txt30Second.visibility = View.GONE
            binding.txtNotiTimer.text = getString(R.string.time_noti, mimutes.toString())
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000L)
            action.backToBubble()
        }
    }


    // private func --------------------------------------------------------------------------------

    private val customExpandableTimerNotiViewListener =
        object : ExpandableTimerNotification.Action {
            override fun backToBubble() {
                tryRemoveTimerNotiView()
                tryShowBubbles()
            }
        }

    private
    val customExpandableDrawingToolViewListener =
        object : ExpandableDrawingToolView.Action {
            override fun backToBubble() {
                tryRemoveDrawingToolView()
                tryShowBubbles()
            }
        }

    private val customExpandableMenuViewListener = object : ExpandableMenuView.Action {

        override fun popToBubble() {
            tryRemoveExpandableView()
            tryShowBubbles()
        }

        override fun navigateToTimerNoti() {
            Timber.d("navigateToTimerNotification")
            updateTimerNotiView()
            tryNavigateToTimerNotiView()

        }

        override fun navigateToDrawingToolView() {
            Timber.d("navigateToDrawingToolView")
            updateDrawingToolView()
            tryNavigateToDrawingToolView()
        }

        override fun onCameraPreview() {
            if (isHaveCameraPermission()) {
                CameraPreviewService.start(context)
            } else {
                requestCameraPermission(ctx as Activity)
            }
        }
    }

    private val customFloatingBubbleTouchEvent = object : FloatingBubble.TouchEvent {

        override fun onDestroy() {
            tryStopService()
        }

    }

    private val customFloatingBubbleAction = object : FloatingBubble.Action {

        override fun navigateToExpandableView() {
            tryNavigateToExpandableView()
        }
    }


    private fun tryNavigateToExpandableView() {
        tryShowExpandableView()
            .onComplete {
                tryRemoveBubbles()
            }.onError {
                throw NullViewException("you DID NOT override expandable view")
            }
    }

    private fun tryNavigateToDrawingToolView() {
        tryShowDrawingToolView()
            .onComplete {
                tryRemoveExpandableView()
            }.onError {
                tryShowBubbles()
                    .onError {
                        throw NullViewException("you DID NOT override expandable view")
                    }
            }
    }

    private fun tryNavigateToTimerNotiView() {
        tryShowTimerNotiView()
            .onComplete {
                tryRemoveExpandableView()
            }.onError {
                tryShowBubbles()
                    .onError {
                        throw NullViewException("you DID NOT override expandable view")
                    }
            }
    }


    private fun tryStopService() {

        tryRemoveAllView()
        stopSelf()
    }

    private fun tryRemoveAllView() {
        tryRemoveExpandableView()
        tryRemoveBubbles()
    }

    // shorten -------------------------------------------------------------------------------------
    private fun tryRemoveDrawingToolView() = logIfError {
        drawingToolView?.remove()
    }


    private fun tryShowDrawingToolView() = logIfError {
        drawingToolView?.show()
    }

    private fun tryShowTimerNotiView() = logIfError {
        timerNotiView?.show()
    }

    private fun tryRemoveTimerNotiView() = logIfError {
        timerNotiView?.remove()
    }

    private fun tryRemoveExpandableView() = logIfError {
        expandableMenuView?.remove()
    }

    private fun tryShowExpandableView() = logIfError {
        expandableMenuView?.show()
    }

    private fun tryShowBubbles() = logIfError {
        floatingBubble?.showIcon()
    }

    private fun tryRemoveBubbles() = logIfError(isLog = false) {
        floatingBubble?.removeIcon()
        floatingBubble?.removeRemoveIcon()
    }

    private fun isHigherThanAndroid8() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

}
