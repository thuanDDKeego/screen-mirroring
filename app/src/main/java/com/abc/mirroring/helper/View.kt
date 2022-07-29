package com.abc.mirroring.helper

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver


val Int.toDp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.toPx: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * return: -x | x
 * */
fun View.getXYPointOnScreen(): Point {
    val arr = IntArray(2)
    this.getLocationOnScreen(arr)

    return Point(arr[0], arr[1])
}


fun View.enableDisableViewWithChildren(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1f else .5f
    if (this is ViewGroup) {
        for (idx in 0 until childCount) getChildAt(idx).enableDisableViewWithChildren(enabled)
    }
}

fun View.setMargins(
    leftMarginPx: Int? = null,
    topMarginPx: Int? = null,
    rightMarginPx: Int? = null,
    bottomMarginPx: Int? = null,
) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        leftMarginPx?.run { params.leftMargin = this }
        topMarginPx?.run { params.topMargin = this }
        rightMarginPx?.run { params.rightMargin = this }
        bottomMarginPx?.run { params.bottomMargin = this }
        requestLayout()
    }
}

// exclude view gesture on home screen -------------------------------------------------------------
private var exclusionRects: MutableList<Rect> = ArrayList()

internal fun View.updateGestureExclusion(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return


    val screenSize = ScreenInfo.getScreenSize(context.applicationContext)

    exclusionRects.clear()

    val rect = Rect(0, 0, this.width, screenSize.height)
    exclusionRects.add(rect)


    this.systemGestureExclusionRects = exclusionRects
}

inline fun View.afterMeasured(crossinline afterMeasuredWork: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

        override fun onGlobalLayout() {

            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                afterMeasuredWork()
            }

        }
    })
}