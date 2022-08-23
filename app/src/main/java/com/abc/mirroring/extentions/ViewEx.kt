package com.abc.mirroring.extentions

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator

fun View.getColor(resId: Int): Int = ResourcesCompat.getColor(context.resources, resId, null)

fun View.fadeInAnimation() {
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
    ObjectAnimator.ofPropertyValuesHolder(this, alpha).apply {
        interpolator = LinearOutSlowInInterpolator()
        duration = 250
    }.start()
}

fun View.scaleAnimation() {
    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f, 1f)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f, 1f)
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.8f, 1f)
    ObjectAnimator.ofPropertyValuesHolder(this, alpha, scaleX, scaleY).apply {
        interpolator = LinearOutSlowInInterpolator()
        duration = 300
    }.start()
}

fun View.fadeOutAnimation(onEnd: () -> Unit) {
    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.9f)
    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.9f)
    val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
    ObjectAnimator.ofPropertyValuesHolder(this, alpha, scaleX, scaleY).apply {
        interpolator = LinearOutSlowInInterpolator()
        duration = 250
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                onEnd.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
    }.start()

}

fun View.animValue(start: Float, end: Float, duration: Long, onValueChange: (Float) -> Unit) {
    val delta = end - start
    val animator = ValueAnimator.ofFloat(start, end)
    animator.addUpdateListener {
        val percent = it.animatedFraction
        onValueChange(start + delta * percent)
    }
    animator.duration = duration
    animator.start()
}

fun AppCompatImageView.setTintColor(colorId: Int) {
    setColorFilter(ResourcesCompat.getColor(context.resources, colorId, null))
}

fun AppCompatImageView.setBackgroundTintColor(colorId: Int) {
    backgroundTintList = ResourcesCompat.getColorStateList(context.resources, colorId, null)
}

fun AppCompatTextView.setResTextColor(colorId: Int) {
    setTextColor(ResourcesCompat.getColor(context.resources, colorId, null))
}

fun View.runOnUI(onRun: () -> Unit) {
    Handler(Looper.getMainLooper()).post {
        onRun.invoke()
    }
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}


