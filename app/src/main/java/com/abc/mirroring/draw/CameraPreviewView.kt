package com.abc.mirroring.draw

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.abc.mirroring.R
import com.abc.mirroring.databinding.LayoutCameraPreviewBinding
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException
import kotlin.math.hypot


class CameraPreviewView : ConstraintLayout, LifecycleOwner {
    var eventCloseCamera: (() -> Unit?)? = null
    private lateinit var binding: LayoutCameraPreviewBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private val mLifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context) : super(context) {
        initView()
    }

    private var startX: Float = 0f
    private var startY: Float = 0f
    private var widthView: Int = 0
    private var heightView: Int = 0

    private fun updateSizeView() {
        widthView = binding.previewView.layoutParams.width
        heightView = binding.previewView.layoutParams.height
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        val view = inflate(context, R.layout.layout_camera_preview, this)
        binding = LayoutCameraPreviewBinding.bind(view)
        updateSizeView()

        binding.btnSwitchCamera.setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bindPreview()
        }
        binding.btnCloseCamera.setOnClickListener {
            eventCloseCamera?.invoke()
        }
        binding.btnScaleCamera.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = e.rawX
                    startY = e.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val newR = hypot(e.rawX - startX, e.rawY - startY)
                    val sizeR = if ((e.rawX - startX) < 0) {
                        -newR
                    } else newR
                    binding.previewView.layoutParams.width = widthView + sizeR.toInt()
                    binding.previewView.layoutParams.height = heightView + sizeR.toInt()
                    binding.previewView.invalidate()
                    binding.previewView.requestLayout()
                }
                MotionEvent.ACTION_UP -> {
                    binding.previewView.invalidate()
                    binding.previewView.requestLayout()
                    updateSizeView()
                }
            }
            true
        }
        setCameraProviderListener()
    }

    private fun bindPreview() {
        cameraProvider.unbindAll()
        binding.previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding.previewView.display.rotation)
            .build()
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(lensFacing).build()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(preview)
            .build()

        cameraProvider.bindToLifecycle(this, cameraSelector, preview)
    }

    private fun setCameraProviderListener() {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindPreview()
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future
                // This should never be reached
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    override fun getLifecycle(): Lifecycle = mLifecycleRegistry
}