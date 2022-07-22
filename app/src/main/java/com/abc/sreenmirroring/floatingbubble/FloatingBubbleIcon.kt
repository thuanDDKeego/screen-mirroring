package com.abc.sreenmirroring.floatingbubble

import android.annotation.SuppressLint
import android.graphics.Point
import android.graphics.PointF
import android.util.Size
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.databinding.FloatIconMainBinding
import com.abc.sreenmirroring.helper.*


internal class FloatingBubbleIcon(
    private val bubbleBuilder: FloatingBubble.Builder,
    private val screenSize: Size
) : BaseFloatingView(bubbleBuilder.context) {

    companion object{
        const val MARGIN_PX_FROM_TOP = 100
        const val MARGIN_PX_FROM_BOTTOM = 150
    }


    var _binding: FloatIconMainBinding? = null
    val binding get() = _binding!!


    private val prevPoint = Point(0, 0)
    private val pointF = PointF(0f, 0f)
    private val newPoint = Point(0, 0)

    private val screenHalfWidth = screenSize.width / 2
    private val screenHalfHeight = screenSize.height / 2

    init {

        _binding = FloatIconMainBinding.inflate(LayoutInflater.from(bubbleBuilder.context))

        setupLayoutParams()
        setupIconProperties()
        customTouch()

    }

    /**
     * must be root view
     * */
    fun show() = logIfError {
        super.show(binding.root)
    }

    /**
     * must be root view
     * */
    fun remove() = logIfError {
        super.remove(binding.root)
    }


    fun destroy() {
        _binding = null
    }


    private val myAnimationHelper = Anim()
    private var isAnimating = false
    fun animateIconToEdge(
        offsetPx: Int,        //    68
        onFinished: () -> Unit
    ) {
        if (!isAnimating) {
            isAnimating = true

            val currentIconX = binding.root.getXYPointOnScreen().x

            if (currentIconX < screenHalfWidth - offsetPx) {    // animate icon to the LEFT side

                val realX = screenHalfWidth - currentIconX  // 235
                val leftEdgeX = screenHalfWidth - offsetPx  // 540 - 68 = 472

                myAnimationHelper.startSpringX(
                    realX.toFloat(),
                    leftEdgeX.toFloat(),
                    object : Anim.Event {
                        override fun onUpdate(float: Float) {

                            tryOnly {
                                windowParams!!.x = -(float.toInt())
                                windowManager?.updateViewLayout(binding.root, windowParams)
                            }

                        }

                        override fun onFinish() {
                            isAnimating = false
                            onFinished()
                        }
                    }
                )

            } else {                                            // animate icon to the RIGHT side

                val realX = currentIconX - screenHalfWidth + offsetPx  // 235
                val rightEdgeX = screenHalfWidth - offsetPx            // 540 - 68 = 472

                myAnimationHelper.startSpringX(
                    realX.toFloat(),
                    rightEdgeX.toFloat(),
                    object : Anim.Event {
                        override fun onUpdate(float: Float) {

                            tryOnly {
                                windowParams!!.x = float.toInt()
                                windowManager?.updateViewLayout(binding.root, windowParams)
                            }

                        }

                        override fun onFinish() {
                            isAnimating = false
                            onFinished()
                        }
                    }
                )
            }
        }
    }

    // private func --------------------------------------------------------------------------------

    private fun setupIconProperties() {

        val iconBitmap =
            bubbleBuilder.iconBitmap ?: ContextCompat.getDrawable(bubbleBuilder.context,
                R.drawable.ic_float_bubble)?.toBitmap()

        binding.homeLauncherMainIcon.apply {
            layoutParams.width = bubbleBuilder.bubbleSizePx
            layoutParams.height = bubbleBuilder.bubbleSizePx
        }
//        binding.homeLauncherMainIcon.apply {
//            setImageBitmap(iconBitmap)
//            layoutParams.width = bubbleBuilder.bubbleSizePx
//            layoutParams.height = bubbleBuilder.bubbleSizePx
//
//            elevation = bubbleBuilder.elevation.toFloat()
//
//            alpha = bubbleBuilder.alphaF
//        }

        windowParams?.apply {
            x = bubbleBuilder.startingPoint.x
            y = bubbleBuilder.startingPoint.y
        }

    }


    @SuppressLint("ClickableViewAccessibility")
    private fun customTouch() {

        fun onActionDown(motionEvent: MotionEvent) {
            prevPoint.x = windowParams!!.x
            prevPoint.y = windowParams!!.y

            pointF.x = motionEvent.rawX
            pointF.y = motionEvent.rawY

            bubbleBuilder.listener?.onDown(prevPoint.x, prevPoint.y)
        }

        fun onActionMove(motionEvent: MotionEvent) {
            val mIconDeltaX = motionEvent.rawX - pointF.x
            val mIconDeltaY = motionEvent.rawY - pointF.y

            newPoint.x = prevPoint.x + mIconDeltaX.toInt()  // eg: -X .. X  |> (-540 .. 540)
            newPoint.y = prevPoint.y + mIconDeltaY.toInt()  // eg: -Y .. Y  |> (-1xxx .. 1xxx)

            windowParams!!.x = newPoint.x
            windowParams!!.y = newPoint.y
            update(binding.root)

            bubbleBuilder.listener?.onMove(newPoint.x, newPoint.y)
        }

        fun onActionUp() {
            // prevent bubble's Y coordinate move outside the screen
            if (newPoint.y > screenHalfHeight - MARGIN_PX_FROM_BOTTOM) {
                newPoint.y = screenHalfHeight - MARGIN_PX_FROM_BOTTOM
            } else if (newPoint.y < -screenHalfHeight + MARGIN_PX_FROM_TOP) {
                newPoint.y = -screenHalfHeight + MARGIN_PX_FROM_TOP
            }
            windowParams!!.y = newPoint.y
            update(binding.root)

            bubbleBuilder.listener?.onUp(newPoint.x, newPoint.y)

//                        animateIconToEdge(68) {}
        }


        val gestureDetector = GestureDetector(bubbleBuilder.context, SingleTapConfirm())

        binding.homeLauncherMainIcon.also { imageView ->

            imageView.afterMeasured {
                bubbleBuilder.context.let { nonNullContext ->
                    imageView.updateGestureExclusion(nonNullContext)
                }
            }


            imageView.setOnTouchListener { _, motionEvent ->

                // detect onTouch event first. If event is consumed, return@setOnTouch...
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    bubbleBuilder.listener?.onClick()
                    return@setOnTouchListener true
                }

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> onActionDown(motionEvent)
                    MotionEvent.ACTION_MOVE -> onActionMove(motionEvent)
                    MotionEvent.ACTION_UP -> onActionUp()
                }

                return@setOnTouchListener true
            }
        }
    }

    private class SingleTapConfirm : SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            return true
        }
    }

    // override

    override fun setupLayoutParams() {
        super.setupLayoutParams()

        logIfError {
            windowParams?.apply {

                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//            windowAnimations = R.style.IconStyle
            }

        }


    }
}