package com.abc.mirroring.floatingbubble

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.viewbinding.ViewBinding
import com.abc.mirroring.R
import com.abc.mirroring.databinding.FloatDrawingToolBinding
import com.abc.mirroring.draw.draw_option.data.LineType
import com.abc.mirroring.helper.logIfError
import com.abc.mirroring.helper.onComplete
import timber.log.Timber

class ExpandableDrawingToolView(
    private val builder: BuilderDrawingTool,
) : BaseFloatingView(builder.context) {
    init {
        setupLayoutParams()
    }

    fun show() = logIfError {
        builder.rootView?.let { super.show(it) }
    }.onComplete {
        builder.listener.onOpenDrawingToolView()
    }

    fun remove() = logIfError {
        builder.rootView?.let { super.remove(it) }
    }.onComplete {
        builder.listener.onCloseDrawingToolView()
    }

    override fun setupLayoutParams() {
        super.setupLayoutParams()
        logIfError {
            windowParams?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.TOP
                flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                dimAmount = builder.dim
            }
        }
    }

    interface Action {
        fun backToBubble() {}
        fun onOpenDrawingToolView() {}
        fun onCloseDrawingToolView() {}
        fun choosePents() {}
        fun chooseColors() {}
        fun chooseEraser() {}
    }

    class BuilderDrawingTool : IExpandableDrawingToolViewBuilder {
        lateinit var context: Context

        var rootView: View? = null
        var listener = object : Action {}
        var dim = 0f
        var colorPencilState = MutableLiveData(R.color.draw_black)

        override fun with(context: Context): BuilderDrawingTool {
            this.context = context
            return this
        }

        override fun setColorSate(colorState: MutableLiveData<Int>): BuilderDrawingTool {
            colorPencilState = colorState
            return this
        }

        @SuppressLint("UseCompatLoadingForDrawables", "UseCompatLoadingForColorStateLists")
        override fun setDrawingToolView(floatingViewBinding: ViewBinding): BuilderDrawingTool {
            //set action draw tools
            val binding = floatingViewBinding as FloatDrawingToolBinding
            binding.apply {
                llDrawToolsBox.visibility = View.VISIBLE
                Timber.d("====${colorPencilState.value ?: -1} ${R.color.draw_black}")
                drawView.brushColor = Color.RED
                btnOpenPencil.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        colorPencilState.value ?: R.color.draw_black
                    ),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                txtPenCilSize.text = binding.seekBarPencilSize.progress.toString()
                btnOpenPencil.setOnClickListener {
                    btnOpenPencil.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            colorPencilState.value ?: R.color.draw_black
                        ),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    btnOpenEraser.setColorFilter(
                        ContextCompat.getColor(context, R.color.line_gray),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    drawView.lineType = LineType.SOLID
                }
                btnChooseColor.setOnClickListener {
                    btnOpenPencil.setColorFilter(
                        ContextCompat.getColor(
                            context,
                            colorPencilState.value ?: R.color.draw_black
                        ),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    btnOpenEraser.setColorFilter(
                        ContextCompat.getColor(context, R.color.line_gray),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    llDrawToolsBox.visibility = View.GONE
                    llPencilDraw.visibility = View.VISIBLE
                    drawView.lineType = LineType.SOLID
//                    btnOpenPencil.backgroundTintList =
//                        context.resources.getColorStateList(R.color.blueA01)
                }
                btnOpenEraser.setOnClickListener {
                    llDrawToolsBox.visibility = View.GONE
                    llEraserOptions.visibility = View.VISIBLE
                    btnOpenPencil.setColorFilter(
                        ContextCompat.getColor(context, R.color.line_gray),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    btnOpenEraser.setColorFilter(
                        ContextCompat.getColor(context, R.color.blueA01),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    drawView.lineType = LineType.ERASER
                }
                btnUnDo.setOnClickListener {
                    drawView.undo()
                }
                btnBack.setOnClickListener {
                    listener.backToBubble()
                }
            }


            //set action drawer tools
            initDrawView(binding)
            binding.apply {
                imgChooseBlack.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_black)
                    drawView.brushColor = Color.BLACK
                    colorPencilState.value = R.color.draw_black
                    btnOpenPencil.setColorFilter(
                        ContextCompat.getColor(context, R.color.draw_black),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                imgChooseWhite.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_white)
                    drawView.brushColor = Color.WHITE
                    colorPencilState.value = R.color.draw_white
                    btnOpenPencil.setColorFilter(
                        ContextCompat.getColor(context, R.color.draw_white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                imgChooseBlue.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_blue)
                    drawView.brushColor = context.resources.getColor(R.color.draw_blue)
                    colorPencilState.value = R.color.draw_blue
                    btnOpenPencil.setColorFilter(
                        ContextCompat.getColor(context, R.color.draw_blue),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                imgChooseRed.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_red)
                    drawView.brushColor = context.resources.getColor(R.color.draw_red)
                    colorPencilState.value = R.color.draw_red
                    btnOpenPencil.setColorFilter(
                        ContextCompat.getColor(context, R.color.draw_red),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                imgChooseYellow.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_yellow)
                    drawView.brushColor = context.resources.getColor(R.color.draw_yellow)
                    colorPencilState.value = R.color.draw_yellow
                    btnOpenPencil.setColorFilter(
                        ContextCompat.getColor(context, R.color.draw_yellow),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                imgChoosePink.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_pink)
                    drawView.brushColor = context.resources.getColor(R.color.draw_pink)
                    colorPencilState.value = R.color.draw_pink
                    btnOpenPencil.setColorFilter(
                        ContextCompat.getColor(context, R.color.draw_pink),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                imgChooseOrange.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_orange)
                    drawView.brushColor = context.resources.getColor(R.color.draw_orange)
                    colorPencilState.value = R.color.draw_orange
                    btnOpenPencil.setColorFilter(
                        ContextCompat.getColor(context, R.color.draw_orange),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
                btnAccept.setOnClickListener {
                    binding.llPencilDraw.visibility = View.GONE
                    binding.llDrawToolsBox.visibility = View.VISIBLE
                }
            }
            binding.seekBarPencilSize.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    binding.txtPenCilSize.text = binding.seekBarPencilSize.progress.toString()
                    binding.drawView.brushSize = binding.seekBarPencilSize.progress.toFloat()
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })

            //set eraser tools
            binding.apply {
                txtEraserSize.text = seekBarEraserSize.progress.toString()
                btnEraserAccept.setOnClickListener {
                    llEraserOptions.visibility = View.GONE
                    llDrawToolsBox.visibility = View.VISIBLE
                    btnClearAll.setOnClickListener { drawView.clearCanvas(true) }
                }
                seekBarEraserSize.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        txtEraserSize.text = binding.seekBarEraserSize.progress.toString()
                        drawView.eraserSize = binding.seekBarEraserSize.progress.toFloat()
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }
                })
            }


            this.rootView = binding.root
            return this
        }

        private fun initDrawView(binding: FloatDrawingToolBinding) {
            initDrawCallback(binding)
            binding.drawView.apply {
                lineType = LineType.SOLID
                brushColor =
                    context.resources.getColor(colorPencilState.value ?: R.color.draw_black)
                brushSize = 30f
            }
        }

        private fun initDrawCallback(binding: FloatDrawingToolBinding) {
//            binding.drawView.apply {
//                drawViewPressCallback = {
//                    hideDrawOptionPanel()
//                    refreshDrawButtonStateByMode()
//                }
//
//                undoStateCallback = { isAvailable ->
//                    binding.undoButton.isClicked = isAvailable
//                }
//
//                redoStateCallback = { isAvailable ->
//                    binding.redoButton.isClicked = isAvailable
//                }
//            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private fun resetPencilChooseColor(binding: FloatDrawingToolBinding) {
            binding.apply {
                (imgChooseBlack as ImageView).setImageResource(R.drawable.ic_circle_black)
                (imgChooseWhite as ImageView).setImageResource(R.drawable.ic_circle_white)
                (imgChooseBlue as ImageView).setImageResource(R.drawable.ic_circle_blue)
                (imgChooseRed as ImageView).setImageResource(R.drawable.ic_circle_red)
                (imgChooseYellow as ImageView).setImageResource(R.drawable.ic_circle_yellow)
                (imgChoosePink as ImageView).setImageResource(R.drawable.ic_circle_pink)
                (imgChooseOrange as ImageView).setImageResource(R.drawable.ic_circle_orange)
            }
        }

        override fun addDrawingToolViewListener(action: Action): BuilderDrawingTool {
            this.listener = action
            return this
        }

        override fun setDimAmount(dimAmount: Float): BuilderDrawingTool {
            this.dim = dimAmount
            return this
        }

        override fun build(): ExpandableDrawingToolView {
            return ExpandableDrawingToolView(this)
        }
    }

    interface IExpandableDrawingToolViewBuilder {

        fun with(context: Context): IExpandableDrawingToolViewBuilder

        fun setColorSate(colorState: MutableLiveData<Int>): IExpandableDrawingToolViewBuilder

        fun setDrawingToolView(binding: ViewBinding): IExpandableDrawingToolViewBuilder

        fun addDrawingToolViewListener(action: ExpandableDrawingToolView.Action): IExpandableDrawingToolViewBuilder

        fun setDimAmount(dimAmount: Float): IExpandableDrawingToolViewBuilder

        fun build(): ExpandableDrawingToolView

    }
}