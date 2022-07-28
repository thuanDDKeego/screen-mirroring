package com.abc.sreenmirroring.draw

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
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.databinding.LayoutCameraPreviewBinding
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException


class CameraPreviewView : ConstraintLayout, LifecycleOwner {
    private lateinit var previewView: PreviewView
    private lateinit var binding: LayoutCameraPreviewBinding
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
        previewView = view.findViewById(R.id.preview_view)

        setCameraProviderListener()
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(previewView.display.rotation)
            .build()
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
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
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
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