package com.abc.mirroring.draw

import android.content.Context
import android.util.AttributeSet
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


class CameraPreviewView : ConstraintLayout, LifecycleOwner {
    var eventCloseCamera: (() -> Unit?)? = null
    private lateinit var binding: LayoutCameraPreviewBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
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

    private fun initView() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        val view = inflate(context, R.layout.layout_camera_preview, this)
        binding = LayoutCameraPreviewBinding.bind(view)
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