package com.abc.mirroring.floatingbubble

import android.graphics.Bitmap
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.abc.mirroring.R
import com.abc.mirroring.databinding.FloatRemoveIconBinding
import com.abc.mirroring.helper.logIfError
import timber.log.Timber

internal class FloatingRemoveBubbleIcon(
    private val bubbleBuilder: FloatingBubble.Builder,
    private val screenSize: Size,
) : BaseFloatingView(bubbleBuilder.context) {

    private var _binding: FloatRemoveIconBinding? = null
    val binding get() = _binding!!


    init {
        _binding = FloatRemoveIconBinding.inflate(LayoutInflater.from(bubbleBuilder.context))
        setupLayoutParams()
        setupRemoveBubbleProperties()
    }


    override fun setupLayoutParams() {
        super.setupLayoutParams()

        logIfError {
            windowParams!!.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.BOTTOM or Gravity.CENTER
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION

//            windowAnimations = R.style.IconStyle
            }
        }


    }

    // public --------------------------------------------------------------------------------------

    fun show() {
        super.show(binding.root)
    }

    fun remove() {
        try {
            super.remove(binding.root)
        }catch (e:Exception){
            Timber.d(e)
        }
    }

    fun destroy() {
        _binding = null
    }

    // private -------------------------------------------------------------------------------------

    private fun setupRemoveBubbleProperties() {
        val icBitmap:Bitmap =
            (bubbleBuilder.iconRemoveBitmap ?: ContextCompat.getDrawable(bubbleBuilder.context, R.drawable.ic_float_remove_icon)) as Bitmap

        binding.homeLauncherMainBinIcon.apply {
            setImageBitmap(icBitmap)
            layoutParams.width = bubbleBuilder.bubbleSizePx
            layoutParams.height = bubbleBuilder.bubbleSizePx

            elevation = bubbleBuilder.elevation.toFloat()

            alpha = bubbleBuilder.alphaF
        }
    }

}