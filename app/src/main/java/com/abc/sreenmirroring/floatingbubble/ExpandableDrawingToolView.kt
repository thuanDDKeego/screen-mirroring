package com.abc.sreenmirroring.floatingbubble

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
import androidx.viewbinding.ViewBinding
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.databinding.FloatDrawingToolBinding
import com.abc.sreenmirroring.draw.draw_option.data.LineType
import com.abc.sreenmirroring.helper.logIfError
import com.abc.sreenmirroring.helper.onComplete

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

        override fun with(context: Context): BuilderDrawingTool {
            this.context = context
            return this
        }

        @SuppressLint("UseCompatLoadingForDrawables", "UseCompatLoadingForColorStateLists")
        override fun setDrawingToolView(floatingViewBinding: ViewBinding): BuilderDrawingTool {
            //set action draw tools
            val binding = floatingViewBinding as FloatDrawingToolBinding
            binding.apply {
                llDrawToolsBox.visibility = View.VISIBLE
                txtPenCilSize.text = binding.seekBarPencilSize.progress.toString()
                btnOpenPencil.setOnClickListener {
                    llDrawToolsBox.visibility = View.GONE
                    llPencilDraw.visibility = View.VISIBLE
                    btnOpenPencil.backgroundTintList =
                        context.resources.getColorStateList(R.color.blueA01)
                }
                btnOpenEraser.setOnClickListener {
                    llDrawToolsBox.visibility = View.GONE
                    llEraserOptions.visibility = View.VISIBLE
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
                }
                imgChooseWhite.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_white)
                    drawView.brushColor = Color.WHITE
                }
                imgChooseBlue.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_blue)
                    drawView.brushColor = context.resources.getColor(R.color.draw_blue)
                }
                imgChooseRed.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_red)
                    drawView.brushColor = context.resources.getColor(R.color.draw_red)
                }
                imgChooseYellow.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_yellow)
                    drawView.brushColor = context.resources.getColor(R.color.draw_yellow)
                }
                imgChoosePink.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_pink)
                    drawView.brushColor = context.resources.getColor(R.color.draw_pink)
                }
                imgChooseOrange.setOnClickListener {
                    resetPencilChooseColor(this)
                    (it as ImageView).setImageResource(R.drawable.ic_circle_choosed_orange)
                    drawView.brushColor = context.resources.getColor(R.color.draw_orange)
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
                btnEraserAccept.setOnClickListener {
                    llEraserOptions.visibility = View.GONE
                    llDrawToolsBox.visibility = View.VISIBLE
                    drawView.lineType = LineType.SOLID
                }
                seekBarEraserSize.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        binding.txtEraserSize.text = binding.seekBarEraserSize.progress.toString()
                        binding.drawView.eraserSize = binding.seekBarEraserSize.progress.toFloat()
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
                brushColor = Color.BLACK
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

        fun setDrawingToolView(binding: ViewBinding): IExpandableDrawingToolViewBuilder

        fun addDrawingToolViewListener(action: ExpandableDrawingToolView.Action): IExpandableDrawingToolViewBuilder

        fun setDimAmount(dimAmount: Float): IExpandableDrawingToolViewBuilder

        fun build(): ExpandableDrawingToolView

    }
}