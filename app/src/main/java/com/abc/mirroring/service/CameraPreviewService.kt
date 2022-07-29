package com.abc.mirroring.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Region
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.abc.mirroring.draw.CameraPreviewView
import timber.log.Timber

class CameraPreviewService : Service() {

    companion object {
        private var mWindowManager: WindowManager? = null
        private lateinit var cameraPreview: CameraPreviewView
        var notificationId = 102

        private lateinit var ctx: Context
        var isRunning: Boolean = false

        fun start(context: Context) {
            ctx = context
            if (!isRunning) {
                val intentFloatToolService = getAppServiceIntent(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForeground(context, intentFloatToolService)
                } else {
                    startService(context, intentFloatToolService)
                }
            }
        }

        fun stop(context: Context) {
            isRunning = false
            mWindowManager?.removeView(cameraPreview)
            val intent = getAppServiceIntent(context)
            context.stopService(intent)
        }

        private fun getAppServiceIntent(context: Context): Intent =
            Intent(context.applicationContext, CameraPreviewService::class.java)

        private fun startService(context: Context, intent: Intent) = context.startService(intent)

        private fun startForeground(context: Context, intent: Intent) =
            ContextCompat.startForegroundService(context, intent)
    }

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "camera_preview_noty_id"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()

            startForeground(notificationId, notification)
        }
        cameraPreview = CameraPreviewView(this)
        cameraPreview.eventCloseCamera = {
            stop(ctx)
        }

        isRunning = true
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val paramsF = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        paramsF.gravity = Gravity.TOP or Gravity.LEFT
        paramsF.x = 0
        paramsF.y = 100
        mWindowManager?.addView(cameraPreview, paramsF)
        updateBoundRegion()
        try {
            cameraPreview.setOnTouchListener(object : View.OnTouchListener {
                var paramsT = paramsF
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f

                private var centerX = 0
                private var centerY = 0
                private var startX = 0
                private var startY = 0
                private var startR = 0
                private var startScale = 0

                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = paramsF.x
                            initialY = paramsF.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            mIsScale = mScaleRegion.contains(event.x.toInt(), event.y.toInt())

                            // calculate center of preview
//                            centerX =
//                                ((cameraPreview.left + cameraPreview.right) / 2f).toInt()
//                            centerY =
//                                ((cameraPreview.top + cameraPreview.bottom) / 2f).toInt()
//                            // recalculate coordinates of starting point
//                            startX = (event.rawX + centerX).toInt()
//                            startY = (event.rawY + centerY).toInt()
//
//                            // get starting distance and scale
//                            startR =
//                                hypot(
//                                    (event.rawX - startX).toDouble(),
//                                    (event.rawY - startY).toDouble()
//                                ).toFloat().toInt()
//                            startScale = cameraPreview.scaleX.toInt()
                        }
                        MotionEvent.ACTION_UP -> {
                            mIsScale = false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            paramsF.x = initialX + (event.rawX - initialTouchX).toInt()
                            paramsF.y = initialY + (event.rawY - initialTouchY).toInt()
                            mWindowManager?.updateViewLayout(v, paramsF)
                            Timber.d("cameraPreview: ${cameraPreview.width} -- ${cameraPreview.height}")
                        }
                    }
                    return false
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun setBoundRegion(region: Region, path: Path) {
        val boundRecF = RectF()
        path.computeBounds(boundRecF, true)
        region.setPath(
            path,
            Region(
                boundRecF.left.toInt(),
                boundRecF.top.toInt(),
                boundRecF.right.toInt(),
                boundRecF.bottom.toInt()
            )
        )
    }

    private fun updateBoundRegion() {
        Path().apply {
            addRect(
                (cameraPreview.width - 100).toFloat(),
                (cameraPreview.height - 100).toFloat(),
                cameraPreview.width.toFloat(),
                cameraPreview.height.toFloat(),
                Path.Direction.CCW
            )
            setBoundRegion(mScaleRegion, this)
        }
    }
}